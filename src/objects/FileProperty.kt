package objects

import javafx.stage.FileChooser

class FileProperty(val filters: List<FileChooser.ExtensionFilter>) {
    var file: String = ""
        set(value){
            field = value
            listener?.onChanged(field)
        }

    interface ChangeListener{
        fun onChanged(file : String)
    }

    var listener : ChangeListener? = null
}