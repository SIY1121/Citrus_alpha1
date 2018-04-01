package interpolation

import annotation.CInterpolation
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

class InterpolatorManager {
    companion object {

        val interpolator: HashMap<String, Class<*>> = HashMap()

        fun load() {
            println("manager")
            val list = ClassLoader.getSystemClassLoader().getResources("interpolation/")
            Files.walkFileTree(Paths.get(list.nextElement().toURI()), object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    val name = file.fileName.toString().replace(".class", "")
                    val clazz = Class.forName("interpolation.$name")
                    if (!clazz.isInterface && clazz.interfaces.contains(Interpolator::class.java) && clazz.annotations[0] is CInterpolation)
                    {
                        interpolator[(clazz.annotations[0] as CInterpolation).name] = clazz
                        println((clazz.annotations[0] as CInterpolation).name)
                    }

                    return super.visitFile(file, attrs)
                }
            })
        }
    }
}