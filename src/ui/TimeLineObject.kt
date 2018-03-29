package ui

import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import objects.CitrusObject
import util.Statics

class TimeLineObject(var cObject : CitrusObject) : Pane(),
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
        //text = name
    }

    enum class EditMode{
        None,Move,IncrementLength,DecrementLength
    }
    interface EditModeChangeListener{
        fun onEditModeChanged(mode : EditMode,offsetX : Double,offsetY : Double)
    }
    var editModeChangeListener : EditModeChangeListener? = null

    fun onMove() {
        cObject.start = (layoutX / TimelineController.pixelPerFrame).toInt()
        cObject.end = ((layoutX + width) / TimelineController.pixelPerFrame).toInt()


        cObject.onLayoutUpdate()
    }

    fun onScaleChanged(){
        layoutX = cObject.start * TimelineController.pixelPerFrame
        prefWidth = cObject.end * TimelineController.pixelPerFrame - layoutX
    }


}