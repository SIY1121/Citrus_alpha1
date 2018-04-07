package objects

import annotation.CDroppable
import annotation.CObject
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.memberProperties

class ObjectManager {
    companion object {
        val list : TreeMap<String,Class<*>> = TreeMap()
        fun load(){
            val v = ClassLoader.getSystemClassLoader().getResources("objects/")
            Files.walkFileTree(Paths.get(v.nextElement().toURI()), object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    val name = file.fileName.toString().replace(".class", "")
                    val clazz = Class.forName("objects.$name")
                    if (clazz !=DrawableObject::class.java && clazz.annotations.any { it is CObject })
                    {
                       println("object:${clazz.name}")
                         list[(clazz.annotations.first { it is CObject } as CObject).name] = clazz
                    }

                    return super.visitFile(file, attrs)
                }
            })
        }

        fun detectObjectByExtension(ex : String):Class<*>?{
            val target = ObjectManager.list .filter {
                it.value.annotations.any{it is CDroppable } && (it.value.annotations.first{it is CDroppable } as CDroppable).filter.any{ex == it}
            }.values
            return if(target.isNotEmpty())
                target.first()
            else
                null
        }
    }
}