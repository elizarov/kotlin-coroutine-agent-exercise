package example

import org.reflections.Reflections
import org.reflections.util.FilterBuilder
import org.reflections.util.ClasspathHelper
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ConfigurationBuilder
import java.util.LinkedList



fun main(args: Array<String>) {
    val timeStart = System.currentTimeMillis()
    // Load all classes from package (http://stackoverflow.com/a/9571146/5338270)
    // Used to measure agent performance.
    val classLoadersList = LinkedList<ClassLoader>()
    classLoadersList.add(ClasspathHelper.contextClassLoader())
    classLoadersList.add(ClasspathHelper.staticClassLoader())

    val reflections = Reflections(ConfigurationBuilder()
            .setScanners(SubTypesScanner(false /* don't exclude Object.class */), ResourcesScanner())
            .setUrls(ClasspathHelper.forClassLoader(*classLoadersList.toTypedArray()))
            .filterInputsBy(FilterBuilder().include(FilterBuilder.prefix("java.awt"))))
    val allClasses = reflections.getSubTypesOf(Any().javaClass)

    println(allClasses.size)
    println(System.currentTimeMillis() - timeStart)
}