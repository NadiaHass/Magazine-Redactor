package com.nadiahassouni.magazinewriter.ui.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.nadiahassouni.magazinewriter.databinding.FragmentUpdateStoryBinding
import com.nadiahassouni.magazinewriter.model.Story
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class UpdateStoryFragment : androidx.fragment.app.Fragment() {
    private var _binding: FragmentUpdateStoryBinding? = null
    private val binding get() = _binding!!
    private var imagesUri: Uri = Uri.parse("")
    private lateinit var storageReference: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentUpdateStoryBinding.inflate(inflater, container, false)

        storageReference = FirebaseStorage.getInstance().getReference("storyPictures")

        val story = arguments?.getSerializable("story") as Story

        putData(story)

        binding.btnUpdateStory.setOnClickListener {
            binding.btnUpdateStory.visibility = View.GONE
            showProgressBar()
            uploadPictureToStorage(imagesUri , story.id , story.imageUrl)
        }

        binding.ivStory.setOnClickListener {
            openFileChooser()
        }

        return binding.root
    }

    private fun putData(story: Story) {
        binding.etTitle.setText(story.title)
        Glide.with(requireContext())
            .load(story.imageUrl)
            .into(binding.ivStory)
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        resultLauncher.launch(intent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data?.data != null) {
                    imagesUri = data.data!!
                    Glide.with(requireContext())
                        .load(imagesUri)
                        .into(binding.ivStory)
                }
            }
        }

    private fun uploadPictureToStorage(uriImage: Uri?, id: String, imageUrl: String) = CoroutineScope(Dispatchers.IO).launch {
        if (uriImage != Uri.parse("")) {
            val fileReference =
                storageReference.child(UUID.randomUUID().toString() + getFileExtension(uriImage!!))
            var image = String()
            fileReference.putFile(uriImage).addOnSuccessListener { l ->
                fileReference.downloadUrl.addOnSuccessListener { uri ->
                    image = uri.toString()

                    val sdf = SimpleDateFormat("yyyy-MM-dd").format(Date())

                    val story = Story(id, image, binding.etTitle.text.toString(), sdf)

                    updateStory(story)
                }
            }
        }else{
            val sdf = SimpleDateFormat("yyyy-MM-dd").format(Date())
            val story = Story(id, imageUrl, binding.etTitle.text.toString(), sdf)

            updateStory(story)

        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun getFileExtension(uriImage: Uri): String? {
        val contentResolver = activity?.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver?.getType(uriImage))
    }

    private fun updateStory(story: Story) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val storyCollectionRef = Firebase.firestore.collection("stories")
            val snapshot = storyCollectionRef.whereEqualTo("id", story.id)
                .get().await()
            for (doc in snapshot.documents) {
                doc.reference.update(mapOf("title" to story.title,
                    "imageUrl" to story.imageUrl, "date" to story.date , "state" to story.state))
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext() , "La story a ete modifiee" , Toast.LENGTH_SHORT).show()
                binding.btnUpdateStory.visibility = View.VISIBLE
                hideProgressBar()
                startActivity(Intent(context , MainActivity::class.java))
                activity?.finish()
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}