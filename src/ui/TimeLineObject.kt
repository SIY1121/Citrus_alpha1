package ui

import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import objects.CitrusObject

class TimeLineObject(var cObject : CitrusObject) : Label(cObject.displayName),
        CitrusObject.DisplayNameChangeListener{

    private val mouseMove = EventHandler<MouseEvent> {
        when {
            it.x < 5 -> {
                scene.cursor = Cursor.H_RESIZE
                editModeChangeListener?.onEditModeChanged(EditMode.DecrementLength,it.x,it.y)
            }
            it.x > width - 5 -> {
                scene.cursor = Cursor.H_RESIZE
                editModeChangeListener?.onEditModeChanged(EditMode.IncrementLength,it.x,it.y)
            }
            else -> {
                scene.cursor = Cursor.DEFAULT
                editModeChangeListener?.onEditModeChanged(EditMode.Move,it.x,it.y)
            }
        }
    }

    private val mouseExited = EventHandler<MouseEvent> {
        scene.cursor = Cursor.DEFAULT
    }

    init {
        cObject.displayNameChangeListener = this
        onMouseMoved = mouseMove
        onMouseExited = mouseExited
    }

    override fun onDisplayNameChanged(name: String) {
        text = name
    }

    enum class EditMode{
        None,Move,IncrementLength,DecrementLength
    }
    interface EditModeChangeListener{
        fun onEditModeChanged(mode : EditMode,offsetX : Double,offsetY : Double)
    }
    var editModeChangeListener : EditModeChangeListener? = null


    fun onLayoutUpdate() {
        cObject.onLayoutUpdate()
    }


}