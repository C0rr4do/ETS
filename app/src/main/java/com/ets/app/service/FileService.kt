package com.ets.app.service

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileService @Inject constructor(@ApplicationContext private val context: Context) {
    val plansSubPath = "plans"

    private val _planNames = MutableLiveData<List<String>>()
    val planNames: LiveData<List<String>>
        get() = _planNames

    private val _reloading = MutableLiveData(false)
    val reloading: LiveData<Boolean>
        get() = _reloading

    val latestPlanName = Transformations.map(planNames) { files -> files.firstOrNull() }

    /**
     * Request all planNames to be reloaded
     */
    suspend fun requestPlanNamesReload(): Boolean {
        if (reloading.value == false) {
            // Set reloading to true, in order to prevent concurrent requests
            _reloading.postValue(true)

            withContext(Dispatchers.IO) {
                // Retrieve list of plan-names from fileSystem and post new list, so UI can be updated
                _planNames.postValue(reloadPlanNamesFromFileSystem())

                // Set reloading to false after request has been processed
                _reloading.postValue(false)
            }

            // Return 'true' because request is being processed
            return true
        } else {
            // Return 'false' because request was not processed (because another request was running already)
            return false
        }
    }

    /**
     * @return true, only if corresponding file was found and deleted
     */
    suspend fun deletePlanFile(planName: String): Boolean {
        return withContext(Dispatchers.IO) {
            getFileByName(planName)?.run {
                delete()
                return@withContext true
            } ?: return@withContext false
        }
    }

    suspend fun onDownloadFinished() {
        requestPlanNamesReload()
    }

    /**
     * @return File that corresponds to [name] or null if no corresponding file is found
     */
    fun getFileByName(name: String?): File? {
        val file = plansDirectory().resolve("$name.pdf")
        return if (name != null && file.exists()) file else null
    }

    /**
     * Also deletes all duplicate plan-files
     *
     * @return All planNames of locally stored plans
     */
    private fun reloadPlanNamesFromFileSystem(): List<String> {
        // Load all files from filesystem
        val loadedFiles =
            plansDirectory().listFiles()?.toMutableList()

        if (!loadedFiles.isNullOrEmpty()) {
            // Sort and reverse (the first planName will be the of the latest plan)
            loadedFiles.sort()
            loadedFiles.reverse()

            val deletedFiles = mutableSetOf<File>()

            loadedFiles.forEach { file1 ->
                // Find duplicate files
                loadedFiles.forEach { file2 ->
                    if (file1 != file2 // File-objects do not refer to the same file on the filesystem
                        && file1.exists() // file1 exists
                        && file2.exists() // file2 exists
                        && filesAreEqual(file1, file2) // file1 and file2 are contentEqual
                    ) {
                        // Add file to 'duplicateFiles' so that it can be deleted later on
                        deletedFiles.add(file2)
                        file2.delete()
                    }
                }
            }
            // Remove all files from 'loadedFiles' that were deleted (because they were duplicates)
            loadedFiles.removeAll(deletedFiles)
        }
        // Map remaining (non-duplicate) files to their names
        val loadedPlanNames = loadedFiles?.map { file -> file.nameWithoutExtension }

        return loadedPlanNames ?: listOf()
    }

    private fun plansDirectory(): File {
        val plansDirectory =
            File(context.getExternalFilesDir(null)?.absolutePath + File.separator + plansSubPath)
        if (!plansDirectory.exists()) {
            plansDirectory.mkdirs()
        }

        Timber.i(plansDirectory.listFiles()?.joinToString { file -> file.nameWithoutExtension })

        return plansDirectory
    }

    private fun filesAreEqual(file1: File, file2: File): Boolean {
        BufferedInputStream(FileInputStream(file1)).use { fis1 ->
            BufferedInputStream(FileInputStream(file2)).use { fis2 ->
                var temp: Int
                // While fis1 has not reached end,
                // read the next byte and store it in the 'temp'-variable
                while (fis1.read().also { temp = it } != -1) {
                    // Compare 'temp' from 'fis1' to the next byte in fis2
                    if (temp != fis2.read()) {
                        return false
                    }
                }
                // Check if 'fis2' is not finished yet
                // If it would be, than it would be larger and thus not equal
                return fis2.read() == -1
            }
        }
    }
}