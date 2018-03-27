package ui

import javafx.scene.control.Label
import objects.CitrusObject

class TimeLineObject(val type: Type) : Label(type.toName()) {
    enum class Type {
        Text, Image, Video;

        fun toName(): String {
            return when (this) {
                Type.Text -> "テキスト"
                Type.Image -> "画像"
                Type.Video -> "動画"
            }
        }
    }

    lateinit var cObject: CitrusObject

    init {

    }

    fun onUpdate() {

    }

}