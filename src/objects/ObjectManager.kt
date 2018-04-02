package objects

import annotation.CObject
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

class ObjectManager {
    companion object {
        val list : TreeMap<String,Class<*>> = TreeMap()
        fun load(){
            val v = ClassLoader.getSystemClassLoader().getResources("objects/")
            Files.walkFileTree(Paths.get(v.nextElement().toURI()), object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    val name = file.fileName.toString().replace(".class", "")
                    val clazz = Class.forName("objects.$name")
                    if (clazz !=DrawableObject::class.java && clazz.annotations.isNotEmpty() && clazz.annotations[0].annotationClass == CObject::class)
                    {
                       println("object:${clazz.name}")
                         list[(clazz.annotations[0] as CObject).name] = clazz
                    }

                    return super.visitFile(file, attrs)
                }
            })
        }
    }
}