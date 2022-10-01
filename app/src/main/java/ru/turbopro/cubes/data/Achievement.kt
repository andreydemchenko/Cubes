package ru.turbopro.cubes.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.HashMap

@Parcelize
@Entity(tableName = "achievements")
data class Achievement @JvmOverloads constructor(
    @PrimaryKey
    var achievId: String = "",
    var name: String = "",
    var points: Int = 0,
    var needPoints: Int = 0,
    var type: String = "",
    var achievImageUrl: String = ""
) : Parcelable {
    fun toHashMap(): HashMap<String, Any> {
        return hashMapOf(
            "achievId" to achievId,
            "name" to name,
            "points" to points,
            "needPoints" to needPoints,
            "type" to type,
            "achievImageUrl" to achievImageUrl
        )
    }
}