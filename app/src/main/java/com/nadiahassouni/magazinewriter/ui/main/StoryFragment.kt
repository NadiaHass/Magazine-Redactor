package com.nadiahassouni.magazinewriter.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nadiahassouni.magazinewriter.R
import com.nadiahassouni.magazinewriter.databinding.FragmentStoryBinding
import com.nadiahassouni.magazinewriter.model.Story
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class StoryFragment : Fragment() {
    private var _binding : FragmentStoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View{
        _binding = FragmentStoryBinding.inflate(inflater , container , false)


        val story = arguments?.getSerializable("story") as Story
        putData(story)

        binding.btnDeleteStory.setOnClickListener {
            val alertDialog = AlertDialog.Builder(requireContext())
                .setTitle("Suppression de la story")
                .setMessage("Est ce que vous voulez vraiment supprimer la story ?")
                .setPositiveButton("Oui") { p0, p1 ->
                    deleteStory(story.id)
                }
                .setNegativeButton("Non"){ p0, p1 ->
                    p0.dismiss()
                }
            alertDialog.show()
        }

        binding.btnUpdateStory.setOnClickListener {
            navigateToUpdateFragment(story)
        }
        return binding.root
    }

    private fun navigateToUpdateFragment(story: Story) {
        val bundle = Bundle()
        bundle.putSerializable("story" , story)
        Navigation.findNavController(binding.root).navigate(R.id.action_storyFragment_to_updateStoryFragment , bundle)
    }

    private fun deleteStory(id: String)= CoroutineScope(Dispatchers.IO).launch {
        try {
            val storyCollectionRef =
                Firebase.firestore.collection("stories").whereEqualTo("id", id )
            val snapshot =  storyCollectionRef.get().await()
            for (doc in snapshot.documents){
                doc.reference.delete()
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "La story a ete supprimee", Toast.LENGTH_LONG).show()
                startActivity(Intent(context , MainActivity::class.java))
                activity?.finish()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun putData(story: Story) {
        binding.tvTitle.text = story.title
        binding.tvDate.text = story.date
        Glide.with(requireContext())
            .load(story.imageUrl)
            .into(binding.ivStory)

    }
}