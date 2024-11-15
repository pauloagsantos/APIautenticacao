package ipt.dam.api.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ipt.dam.api.R
import ipt.dam.api.model.APIResult
import ipt.dam.api.model.Note
import ipt.dam.api.model.TokenJWT
import ipt.dam.api.retrofit.RetrofitInitializer
import ipt.dam.api.ui.adapter.NoteListAdapter
import okhttp3.Credentials
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Thread.sleep
import kotlin.random.Random

class NoteListActivity : AppCompatActivity(), Runnable {

    private var t:Thread = Thread(this)

    private var token:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_list)



        val btnListNotes: Button = findViewById(R.id.btnListNotes)
        btnListNotes.setOnClickListener {
            listNotes()
        }

        val btnAddNote: Button = findViewById(R.id.btnAddNote)
        btnAddNote.setOnClickListener {
            addDummyNote()
        }

        val btnReset: Button = findViewById(R.id.btnReset)
        btnReset.setOnClickListener {
            reset()
        }

        val btnClear: Button = findViewById(R.id.btnClear)
        btnClear.setOnClickListener {
            configureList(emptyList())
        }

        val btnGetNotesBA: Button = findViewById(R.id.btnListNotesBA)
        btnGetNotesBA.setOnClickListener {
            listNotesBA()
        }

        val btnGetNotesJWT: Button = findViewById(R.id.btnListNotesJWT)
        btnGetNotesJWT.setOnClickListener {
            listNotesJWT();
        }

        //t.start()
    } // oncreate

    private fun listNotes() {
        processNotes(RetrofitInitializer().noteService().list())
    }

    private fun listNotesBA() {
        processNotes(RetrofitInitializer().noteService().listBA( Credentials.basic("admin", "admin")))
    }

    private fun listNotesJWT() {
        if (token.equals("") )
             loginJWT("admin", "admin") {
                token = it?.token.toString()
                Toast.makeText(this,"Token " + it?.token,Toast.LENGTH_SHORT).show()
                val call = RetrofitInitializer().noteService().listJWT(token = "Bearer "+it?.token);
                processNotes(call)
            }
        else {
            val call = RetrofitInitializer().noteService().listJWT(token = "Bearer "+token);
            processNotes(call)
            Toast.makeText(this,"Use previous Token " + token,Toast.LENGTH_SHORT).show()

        }

    }

    private fun loginJWT(username: String, password: String,  onResult: (TokenJWT?) -> Unit){
        val call = RetrofitInitializer().noteService().loginJWT(username, password)
        call.enqueue(
            object : Callback<TokenJWT> {
                override fun onFailure(call: Call<TokenJWT>, t: Throwable) {
                    t.printStackTrace()
                    onResult(null)
                }
                override fun onResponse( call: Call<TokenJWT>, response: Response<TokenJWT>) {
                    val tokenResult = response.body()
                    onResult(tokenResult)
                }
            }
        )
    }

    private fun reset() {
        processNotes(RetrofitInitializer().noteService().reset())
    }

    private fun processNotes(call:Call<List<Note>>) {
        call.enqueue(object : Callback<List<Note>?> {
            override fun onResponse(call: Call<List<Note>?>?,
                                    response: Response<List<Note>?>?) {
                response?.body()?.let {
                    val notes: List<Note> = it
                    configureList(notes)
                }
            }

            override fun onFailure(call: Call<List<Note>?>?, t: Throwable?) {
                t?.message?.let { Log.e("onFailure error", it) }
            }
        })
    }

    private fun configureList(notes: List<Note>) {
        val recyclerView: RecyclerView = findViewById(R.id.note_list_recyclerview)
        recyclerView.adapter = NoteListAdapter(notes, this)
        val layoutManager = StaggeredGridLayoutManager( 2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager
    }

    fun addDummyNote() {
        val i = Random.nextInt(100)
        val note = Note("Note "+ i, "Descrition " + i )
        addNote(note) {
            Toast.makeText(this,"Add " + it?.description,Toast.LENGTH_SHORT).show()
            listNotes()
        }
    }

    private fun addNote(note: Note, onResult: (APIResult?) -> Unit){
        val call = RetrofitInitializer().noteService().addNote(note.title, note.description)
        call.enqueue(
            object : Callback<APIResult> {
                override fun onFailure(call: Call<APIResult>, t: Throwable) {
                    t.printStackTrace()
                    onResult(null)
                }
                override fun onResponse( call: Call<APIResult>, response: Response<APIResult>) {
                    val addedNote = response.body()
                    onResult(addedNote)
                }
            }
        )

    }

    override fun run() {
        while (true) {
            listNotes()
            t.run { sleep(1000) }
        }
    }

}