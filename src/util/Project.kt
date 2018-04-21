package util

import objects.CitrusObject

class Project {
    var initialized = false
    val Layer : MutableList<MutableList<CitrusObject>> = ArrayList()
    val width = 1920
    val height = 1080
    val fps = 60
}