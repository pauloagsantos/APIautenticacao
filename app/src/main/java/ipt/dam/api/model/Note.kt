package ipt.dam.api.model

import com.google.gson.annotations.SerializedName

data class Note (
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?
)
