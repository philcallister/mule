package org.mule.gradle;

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import java.util.regex.Pattern

class ContentsVerifierTask extends DefaultTask
{
    @InputFile
    def File whitelist

    @InputDirectory
    def File projectOutputFolder

    @TaskAction
    def run() {
        def Set mandatoryWildcards = []
        def List whitelistEntries = []
        def productVersion = project.version

        whitelist.eachLine() {
            if (!it.startsWith('#') && it.trim().size() != 0) {
                // canonicalize and interpolate the entry
                whitelistEntries << it.replaceAll("\\\\", "/").replaceAll(Pattern.quote('${productVersion}'), productVersion)
            }
        }

        mandatoryWildcards = whitelistEntries.findAll {
            it.endsWith('+')
        }

        // wildcards will be checked explicitly, move them out of the way for regular validation
        whitelistEntries.removeAll(mandatoryWildcards)

        // strip the trailing + sign
        mandatoryWildcards = mandatoryWildcards.collect {
            it - '+'
        }


        def files = gatherFileNames(projectOutputFolder)

        def missing = findMissing(whitelistEntries, files)
        def unexpected = findUnexpected(whitelistEntries, mandatoryWildcards, files)
        def duplicates = findDuplicates(files)

        if (missing || unexpected || duplicates) {
            def msg = new StringBuilder("The following problems have been encountered:\n\n")
            if (missing) {
                msg << "\tMissing from the Distribution:\n"
                missing.eachWithIndex { name, i ->
                    msg << "\t\t\t${(i + 1).toString().padLeft(3)}. ${name}\n"
                }
            }
            if (unexpected) {
                msg << "\tUnexpected entries in the Distribution:\n"
                unexpected.eachWithIndex { name, i ->
                    msg << "\t\t\t${(i + 1).toString().padLeft(3)}. ${name}\n"
                }
            }
            if (duplicates) {
                msg << "\tDuplicate entries in the Distribution:\n"
                duplicates.eachWithIndex { name, i ->
                    msg << "\t\t\t${(i + 1).toString().padLeft(3)}. ${name}\n"
                }
            }
            throw new IllegalStateException(msg as String)
        }
    }

    // list all files, normalizing timestamps for SNAPSHOTs
    def gatherFileNames(root) {
        def canonicalRootPath = root.canonicalPath.replaceAll("\\\\", "/")

        def files = []
        root.eachFileRecurse() { file ->
            def relativePath = file.canonicalPath.replaceAll("\\\\", "/") - canonicalRootPath
            files << relativePath.replaceAll("-\\d{8}.\\d{6}-\\d+", "-SNAPSHOT")
        }
        files
    }

    def findMissing(expected, files) {
        expected.findAll { !files.contains(it) }
                .sort { it.toLowerCase() }
    }

    def findUnexpected(expected, wildcards, actualNames) {
        // find all entries not in the whitelist and prefixed with wildcards
        actualNames.findAll {
            !expected.contains(it) &&
            (wildcards.find { w -> it.startsWith(w)} == null)
        }.sort { it.toLowerCase() }
    }

    def findDuplicates(actualNames) {
        // convert Enumeration -> List and extract zip entry names
        def entries = actualNames.collect { it.substring(it.lastIndexOf("/")+1) }.findAll { it != "" }

        entries.findAll {
            entries.count(it) > 1
        }.unique().sort { it.toLowerCase() } // sort case-insensitive
    }
}