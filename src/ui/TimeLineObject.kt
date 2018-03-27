package ui

import javafx.scene.control.Label
import objects.CitrusObject

class TimeLineObject(var cObject : CitrusObject) : Label(cObject.displayName),CitrusObject.DisplayNameChangeListener {

    init {
        cObject.displayNameChangeListener = this
    }

    override fun onDisplayNameChanged(name: String) {
        text = name
    }

    fun onUpdate() {

    }

}