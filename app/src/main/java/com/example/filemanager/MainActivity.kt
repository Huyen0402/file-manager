package com.example.filemanager

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import org.apache.commons.io.FileUtils

class MainActivity : AppCompatActivity() {
    private lateinit var currentDirectory: File
    private lateinit var fileList: MutableList<String>
    private lateinit var fileListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_management)

        fileListView = findViewById(R.id.file_list_view)
        fileListView.setOnItemClickListener { _, _, position, _ ->
            val selectedFile = fileList[position]
            val file = File(currentDirectory, selectedFile)

            if (isTextFile(file)) {
                displayTextFile(file)
            } else if (isImageFile(file)) {
                displayImageFile(file)
            }
        }

        registerForContextMenu(fileListView)

        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 0)
        } else {
            loadExternalStorage()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadExternalStorage()
        } else {
            // Handle permission denied
        }
    }

    private fun loadExternalStorage() {
        val externalStorage = Environment.getExternalStorageDirectory()
        currentDirectory = externalStorage
        displayFiles(currentDirectory)
    }

    private fun displayFiles(directory: File) {
        currentDirectory = directory
        fileList = directory.list()?.toMutableList() ?: mutableListOf()
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fileList)
        fileListView.adapter = arrayAdapter
    }

    private fun displayTextFile(file: File) {
        // Implement code to display text file content
    }

    private fun displayImageFile(file: File) {
        // Implement code to display image file
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_menu_file, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val selectedFile = fileList[info.position]
        val file = File(currentDirectory, selectedFile)

        return when (item.itemId) {
            R.id.menu_rename_file -> {
                renameFile(file)
                true
            }
            R.id.menu_delete_file -> {
                deleteFile(file)
                true
            }
            R.id.menu_copy_file -> {
                copyFile(file)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_new_folder -> {
                createNewFolder()
                true
            }
            R.id.menu_new_text_file -> {
                createNewTextFile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun isTextFile(file: File): Boolean {
        val extension = getFileExtension(file)
        return extension.equals("txt", ignoreCase = true)
    }

    private fun isImageFile(file: File): Boolean {
        val extension = getFileExtension(file)
        return extension.equals("bmp", ignoreCase = true) ||
                extension.equals("jpg", ignoreCase = true) ||
                extension.equals("png", ignoreCase = true)
    }

    private fun getFileExtension(file: File): String {
        val name = file.name
        val lastDotIndex = name.lastIndexOf(".")
        return if (lastDotIndex != -1) {
            name.substring(lastDotIndex + 1)
        } else {
            ""
        }
    }

    private fun renameFile(file: File) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Rename File")
        builder.setMessage("Enter new file name")

        builder.setPositiveButton("Rename") { dialog, _ ->
            val input = (dialog as AlertDialog).findViewById<EditText>(android.R.id.input)
            val newFileName = input?.text.toString()

            if (newFileName.isNotEmpty()) {
                val newFile = File(file.parent, newFileName)
                if (file.renameTo(newFile)) {
                    displayFiles(currentDirectory)
                }
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun deleteFile(file: File) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete File")
        builder.setMessage("Are you sure you want to delete this file?")

        builder.setPositiveButton("Delete") { _, _ ->
            if (file.delete()) {
                displayFiles(currentDirectory)
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun copyFile(file: File) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Copy File")
        builder.setMessage("Enter destination folder path")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Copy") { dialog, _ ->
            val destinationPath = input.text.toString()
            if (destinationPath.isNotEmpty()) {
                val destinationDirectory = File(destinationPath)
                if (destinationDirectory.isDirectory) {
                    try {
                        FileUtils.copyFileToDirectory(file, destinationDirectory)
                    } catch (e: IOException) {
                        // Handle file copy error
                    }
                }
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun createNewFolder() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Create New Folder")
        builder.setMessage("Enter folder name")

        builder.setPositiveButton("Create") { dialog, _ ->
            val input = (dialog as AlertDialog).findViewById<EditText>(android.R.id.input)
            val folderName = input?.text.toString()

            if (folderName.isNotEmpty()) {
                val newFolder = File(currentDirectory, folderName)
                if (newFolder.mkdir()) {
                    displayFiles(currentDirectory)
                }
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun createNewTextFile() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Create New Text File")
        builder.setMessage("Enter file name")

        builder.setPositiveButton("Create") { dialog, _ ->
            val input = (dialog as AlertDialog).findViewById<EditText>(android.R.id.input)
            val fileName = input?.text.toString()

            if (fileName.isNotEmpty()) {
                val newFile = File(currentDirectory, "$fileName.txt")
                try {
                    newFile.createNewFile()
                    displayFiles(currentDirectory)
                } catch (e: IOException) {
                    // Handle file creation error
                }
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}