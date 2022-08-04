package com.nadiahassouni.magazinewriter.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.nadiahassouni.magazinewriter.R
import com.nadiahassouni.magazinewriter.adapters.ArticleAdapter
import com.nadiahassouni.magazinewriter.adapters.StoryAdapter
import com.nadiahassouni.magazinewriter.databinding.FragmentHomeBinding
import com.nadiahassouni.magazinewriter.model.Article
import com.nadiahassouni.magazinewriter.model.Story
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var articlesList: ArrayList<Article>
    private lateinit var storiesList: ArrayList<Story>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View{
        _binding = FragmentHomeBinding.inflate(inflater , container , false)

        binding.fabArticle.setOnClickListener {
            navigateToAddArticleFragment()
        }

        binding.fabStory.setOnClickListener {
            navigateToAddStoryFragment()
        }
        showProgressBar()
        getArticles()
        getStories()

        return binding.root
    }

    private fun navigateToAddStoryFragment() {
        Navigation.findNavController(binding.root).navigate(R.id.action_homeFragment_to_addStoryFragment)
    }

    private fun navigateToAddArticleFragment() {
        Navigation.findNavController(binding.root).navigate(R.id.action_homeFragment_to_addArticleFragment)
    }

    private fun getArticles() = CoroutineScope(Dispatchers.IO).launch {
        var article : Article?

        try {
            val articleCollectionRef = Firebase.firestore.collection("articles")
            val querySnapshot = articleCollectionRef.get().await()
//                .whereEqualTo("publishingState" , "valide")
            var list = ArrayList<Article>()
            for(doc in querySnapshot.documents){
                article = doc.toObject<Article>()
                list.add(article!!)
            }
            withContext(Dispatchers.Main){
                articlesList = list
                val adapter = ArticleAdapter (requireContext() , articlesList)
                adapter.notifyDataSetChanged()
                binding.rvArticles.layoutManager = LinearLayoutManager(context , LinearLayoutManager.HORIZONTAL , false)
                binding.rvArticles.adapter = adapter
                hideProgressBar()
            }

        }catch (e : java.lang.Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(context , e.message , Toast.LENGTH_LONG ).show()
            }
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }
    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }
    private fun getStories() = CoroutineScope(Dispatchers.IO).launch {
        var story : Story?

        try {
            val storiesCollectionRef = Firebase.firestore.collection("stories")
            val querySnapshot = storiesCollectionRef.get().await()
//                .whereEqualTo("publishingState" , "valide")
            var list = ArrayList<Story>()
            for(doc in querySnapshot.documents){
                story = doc.toObject<Story>()
                list.add(story!!)
            }
            withContext(Dispatchers.Main){
                storiesList = list
                val adapter = StoryAdapter (requireContext() , storiesList)
                adapter.notifyDataSetChanged()
                binding.rvStory.layoutManager = LinearLayoutManager(context , LinearLayoutManager.HORIZONTAL , false)
                binding.rvStory.adapter = adapter
                hideProgressBar()
            }

        }catch (e : java.lang.Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(context , e.message , Toast.LENGTH_LONG ).show()
            }
        }
    }



}