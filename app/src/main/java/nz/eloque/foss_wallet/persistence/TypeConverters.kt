package nz.eloque.foss_wallet.persistence

import android.location.Location
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.MembershipCard
import nz.eloque.foss_wallet.model.PassColors
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.TransitType
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.utils.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.UUID

class TypeConverters {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Uri::class.java, object : TypeAdapter<Uri>() {
            override fun write(out: JsonWriter, value: Uri?) {
                out.value(value?.toString())
            }

            override fun read(input: JsonReader): Uri? {
                return input.nextString()?.let { Uri.parse(it) }
            }
        })
        .create()

    @TypeConverter
    fun fromInstant(instant: Instant): Long {
        return instant.toEpochMilli()
    }

    @TypeConverter
    fun toInstant(instant: Long) : Instant {
        return Instant.ofEpochMilli(instant)
    }

    @TypeConverter
    fun fromColor(colors: PassColors): String {
        return "${colors.background.toArgb()},${colors.foreground.toArgb()},${colors.label.toArgb()}"
    }

    @TypeConverter
    fun toColor(colors: String): PassColors {
        val split = colors.split(",")
        return PassColors(Color(split[0].toInt()), Color(split[1].toInt()), Color(split[2].toInt()))
    }

    @TypeConverter
    fun fromUuid(uuid: UUID): String = uuid.toString()

    @TypeConverter
    fun toUuid(uuid: String): UUID = UUID.fromString(uuid)

    @TypeConverter
    fun fromPassType(passType: PassType): String {
        return when (passType) {
            is PassType.Boarding -> passType.jsonKey + "," + passType.transitType.toString()
            is PassType.Coupon -> passType.jsonKey
            is PassType.Event -> passType.jsonKey
            is PassType.Generic -> passType.jsonKey
            is PassType.StoreCard -> passType.jsonKey
            is PassType.MembershipCard -> passType.jsonKey
            is PassType.FlightPass -> passType.jsonKey
        }
    }

    @TypeConverter
    fun toPassType(passType: String): PassType {
        val split = passType.split(",")
        return if (split.size > 1) {
            PassType.Boarding(TransitType.valueOf(split[1]))
        } else {
            when (passType) {
                PassType.EVENT -> PassType.Event()
                PassType.COUPON -> PassType.Coupon()
                PassType.STORE_CARD -> PassType.StoreCard()
                PassType.MEMBERSHIP_CARD -> PassType.MembershipCard()
                PassType.FLIGHT_PASS -> PassType.FlightPass()
                else -> PassType.Generic()
            }
        }
    }

    @TypeConverter
    fun fromLocations(locations: List<Location>): String {
        val json = JSONArray()
        locations.forEach {
            val locJson = JSONObject()
            locJson.put("latitude", it.latitude)
            locJson.put("longitude", it.longitude)
            json.put(locJson)
        }
        return json.toString()
    }

    @TypeConverter
    fun toLocations(str: String): List<Location> {
        return JSONArray(str).map {
            val location = Location("")
            location.latitude = it.getDouble("latitude")
            location.longitude = it.getDouble("longitude")
            location
        }
    }

    @TypeConverter
    fun fromBarcodes(barcodes: Set<BarCode>): String {
        val json = JSONArray()
        barcodes.forEach { json.put(it.toJson()) }
        return json.toString()
    }

    @TypeConverter
    fun toBarcodes(str: String): Set<BarCode> {
        return JSONArray(str).map { BarCode.fromJson(it) }.toSet()
    }

    @TypeConverter
    fun fromFields(fields: List<PassField>): String {
        val json = JSONArray()
        fields.forEach { json.put(it.toJson()) }
        return json.toString()
    }

    @TypeConverter
    fun toFields(str: String): List<PassField> {
        return JSONArray(str).map { PassField.fromJson(it) }
    }

    @TypeConverter
    fun fromMembershipCard(value: MembershipCard?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toMembershipCard(value: String?): MembershipCard? {
        return gson.fromJson(value, MembershipCard::class.java)
    }

    @TypeConverter
    fun fromUri(uri: Uri?): String? {
        return uri?.toString()
    }

    @TypeConverter
    fun toUri(uriString: String?): Uri? {
        return uriString?.let { Uri.parse(it) }
    }
}
