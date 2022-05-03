package org.openrndr.dokgen

import org.openrndr.dokgen.sourceprocessor.SourceProcessor
import java.io.File
import java.nio.file.Path

/**
 * Returns a relative [Path] between two [File] instances.
 * Potentially could be replaced with `File.toRelativeString()`
 */
fun File.relativeDir(file: File): Path {
    return toPath().relativize(file.toPath()).parent ?: File("").toPath()
}

/**
 * Takes a [Path] and returns a `.` separated String in which
 * non-valid package names are escaped with backticks.
 */
fun Path.escape() = joinToString(".") { part ->
    val str = part.toString()
    val special = str.any { ch -> !ch.isLetter() }
    if (special) "`$str`" else str
}

fun examplesPackageDirective(path: Path): String {
    val pathEscaped = path.escape()
    return "examples" + if(pathEscaped.isNotEmpty()) ".$pathEscaped" else ""
}

object DokGen {
    /**
     * @param ktFileLocation .kt source path relative to project root
     * @param annotations data extracted from annotations found in the .kt file
     * @return valid Jekyll header to inject in a markdown file
     */
    private fun jekyllHeader(
        ktFileLocation: String,
        annotations: Map<String, String>
    ): String {
        val isSectionIndex = ktFileLocation.endsWith("index.kt") ||
                ktFileLocation.endsWith("home.kt")
        val parent = if (isSectionIndex) "~" else annotations["ParentTitle"]

        return """
            ---
            # File generated by dokgen. Do not edit. 
            # Edit '$ktFileLocation' instead.
            layout: default
            title: ${annotations["Title"]}
            parent: $parent
            nav_order: ${annotations["Order"]}
            has_children: $isSectionIndex
            ---
            
        """.trimIndent()
    }

    /**
     * Processes Guide source .kt files. It reads each file and produces
     * multiple files for each input:
     * - a markdown document visible in the online website.
     * - zero or more .kt files to run and produce media
     *   to include in the online website.
     * - zero or more .kt files to upload examples repository in GitHub
     *
     * @param sourceFiles list of .kt files to process
     * @param sourcesRoot root location of the source .kt files
     * @param mdOutputDir location where to write markdown files
     * @param examplesOutputDir location where to write media-producing .kt programs
     * @param examplesForExportOutputDir location where to write .kt examples for GitHub
     * @param webRootUrl URL where GitHub examples will be accessible
     */
    fun processSources(
        sourceFiles: List<File>,
        sourcesRoot: File,
        mdOutputDir: File,
        examplesOutputDir: File,
        examplesForExportOutputDir: File,
        webRootUrl: String?
    ) {
        sourceFiles.forEach { file ->
            when (file.extension) {
                "md" -> throw NotImplementedError(
                    "Latest DokGen supports only .kt files " +
                            "but ${file.absolutePath} was found. " +
                            "Please convert it to .kt."
                )

                "kt" -> {
                    val fileContents = file.readText().replace("\r\n", "\n")

                    val packageDirective = examplesPackageDirective(
                        sourcesRoot.relativeDir(file)
                    )

                    // A. actual dir, name and path (from path on disk)
                    val fileDir = file.parentFile.toRelativeString(sourcesRoot)
                    val fileName = file.nameWithoutExtension
                    val filePath = file.toRelativeString(sourcesRoot)

                    val mkLink = webRootUrl?.let { it ->
                        { index: Int ->
                            val paddedIndex = "$index".padStart(3, '0')
                            "$it/examples/$fileDir/$fileName$paddedIndex.kt"
                        }
                    }

                    val result = try {
                        SourceProcessor.process(
                            fileContents,
                            packageDirective = packageDirective,
                            mkLink = mkLink
                        )
                    } catch(e: IllegalArgumentException) {
                        println("\nError in $filePath")
                        println(e.message)
                        return@forEach
                    }

                    // B. desired dir and name (from annotation)
                    val urlFile = File(result.annotations["URL"]!!)
                    val urlDir = urlFile.parent
                    val urlName = urlFile.name

                    // 1. Write Markdown file
                    val docsOutDir = File(mdOutputDir, urlDir)
                    docsOutDir.mkdirs()

                    val mdTarget = File(docsOutDir, "$urlName.markdown")
                    val fileRelPath = DOCS_DIR + File.separator + filePath
                    val header = jekyllHeader(fileRelPath, result.annotations)
                    mdTarget.writeText(header + result.doc)

                    // 2. Write .kt examples to generate media
                    val examplesOutDir = File(examplesOutputDir, urlDir)
                    examplesOutDir.mkdirs()
                    result.appSources.forEachIndexed { index, s ->
                        val paddedIndex = "$index".padStart(3, '0')
                        // Convert first letter to capital
                        // otherwise JVM doesn't find the class
                        // in `project.javaexec`
                        val urlNameCapital = urlName.replaceFirstChar {
                            it.uppercase()
                        }
                        val sampleOutFile = File(
                            examplesOutDir,
                            "$urlNameCapital$paddedIndex.kt"
                        )
                        println("writing runnable example to $sampleOutFile")
                        sampleOutFile.writeText(s)
                    }

                    // 3. Write .kt examples for GitHub repo
                    val examplesForExportOutDir =
                        File(examplesForExportOutputDir, fileDir)
                    examplesForExportOutDir.mkdirs()
                    result.appSourcesForExport.forEachIndexed { index, s ->
                        val paddedIndex = "$index".padStart(3, '0')
                        val sampleOutFile = File(
                            examplesForExportOutDir,
                            "$fileName$paddedIndex.kt"
                        )
                        println("writing exported example to $sampleOutFile")
                        sampleOutFile.writeText(s)
                    }
                }
            }
        }
    }

    fun getExamplesClassNames(sourceFiles: List<File>, sourcesRoot: File): List<String> {
        return sourceFiles.filter {
            it.extension == "kt"
        }.map { file ->
            val pd = examplesPackageDirective(sourcesRoot.relativeDir(file))
                    .filter { ch -> ch != '`' }
            "$pd.${file.nameWithoutExtension}Kt"
        }
    }
}

