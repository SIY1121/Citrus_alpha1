package ui

import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.PopupControl
import javafx.scene.effect.DropShadow
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import objects.CitrusObject
import objects.MutableProperty
import util.Statics
import javax.swing.Popup
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType

class TimeLineObject(var cObject : CitrusObject) : VBox(),
        CitrusObject.DisplayNameChangeListener{

    val label = Label()
    val popup = PopupControl()
    lateinit var popupRoot : VBox

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

    private val mouseClicked = EventHandler<MouseEvent>{
        if(it.button==MouseButton.SECONDARY){
            popupRoot.children.clear()
            for(p in cObject.javaClass.kotlin.memberProperties)
            {
                val l = Label(p.name + ":" + p.get(cObject).toString())
                l.textFill = Color.WHITE
                popupRoot.children.add(l)

                if(p is KMutableProperty1 && p.name=="displayName"){
                    p.setter.call(cObject,"aa")
                }

            }
            val b = Button("OK")
            b.setOnAction { popup.hide() }
            popupRoot.children.add(b)
            popup.show(this,it.screenX,it.screenY)
            it.consume()
            println("hi")

        }else{
            popup.hide()
        }
    }

    init {
        cObject.displayNameChangeListener = this
        onMouseMoved = mouseMove
        onMouseExited = mouseExited
        onMouseClicked = mouseClicked
        label.maxWidthProperty().bind(widthProperty())
        children.add(label)

        popupRoot = VBox()
        popupRoot.style = "-fx-background-color:#383838AA;-fx-border-radius: 5 5 5 5;-fx-background-radius: 5 5 5 5;"
        popupRoot.effect = DropShadow()
        popupRoot.padding = Insets(10.0)



//        for(c in cObject::class.allSuperclasses){
//            if(c!=Any::class && c!=CitrusObject::class){
//                popupRoot.children.add(Label("-${c.qualifiedName}"))
//                for(p in c.memberProperties)
//                    popupRoot.children.add(Label(p.name))
//            }
//        }


        popup.scene.root = popupRoot
    }

    fun <R: Any?> readPropery(instance: Any, propertyName: String): R {
        val clazz = instance.javaClass.kotlin
        @Suppress("UNCHECKED_CAST")
        return clazz.declaredMemberProperties.first { it.name == propertyName }.get(instance) as R
    }

    override fun onDisplayNameChanged(name: String) {
        label.text = name
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