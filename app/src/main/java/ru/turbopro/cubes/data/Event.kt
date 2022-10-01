package ru.turbopro.cubes.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.HashMap

@Parcelize
@Entity(tableName = "events")
data class Event @JvmOverloads constructor(
    @PrimaryKey
    var eventId: String = "",
    var name: String = "",
    var description: String = "",
    var date: String = "",
    var images: List<String> = ArrayList(),
) : Parcelable {
    fun toHashMap(): HashMap<String, Any> {
        return hashMapOf(
            "eventId" to eventId,
            "name" to name,
            "description" to description,
            "date" to date,
            "images" to images
        )
    }
}