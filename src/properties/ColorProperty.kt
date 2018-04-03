package properties

import javafx.scene.paint.Color

class ColorProperty(def : Color = Color.WHITE) {
    var color : Color = def
        set(value){
            field = value
            listener?.onChanged(field)
        }
    interface ChangeListener{
        fun onChanged(color: Color)
    }
    var listener : ChangeListener? = null
}