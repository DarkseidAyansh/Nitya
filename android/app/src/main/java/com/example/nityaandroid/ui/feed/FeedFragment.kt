package com.example.nityaandroid.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nityaandroid.R
import com.example.nityaandroid.databinding.FragmentFeedBinding
import com.example.nityaandroid.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FeedViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.fabCreatePost.setOnClickListener {
            showCreatePostDialog()
        }
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            onLikeClicked = { postId -> viewModel.toggleLike(postId) },
            onCommentClicked = { postId -> showUnifiedCommentsDialog(postId) }
        )

        binding.rvFeed.adapter = postAdapter

        binding.rvFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3
                    && firstVisibleItemPosition >= 0
                ) {
                    viewModel.loadNextPage()
                }
            }
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.posts.collect { posts ->
                        postAdapter.submitList(posts)
                    }
                }
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    private fun showCreatePostDialog() {
        val input = android.widget.EditText(requireContext()).apply {
            hint = "Share your achievement..."
            setPadding(50, 40, 50, 40)
        }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("New Post")
            .setView(input)
            .setPositiveButton("Post") { _, _ ->
                val content = input.text.toString().trim()
                if (content.isNotEmpty()) {
                    viewModel.createNewPost(content)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showUnifiedCommentsDialog(postId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_comments, null)
        val rvComments = dialogView.findViewById<RecyclerView>(R.id.rvComments)
        val etNewComment = dialogView.findViewById<android.widget.EditText>(R.id.etNewComment)
        val btnSendComment = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSendComment)
        val progressBar = dialogView.findViewById<android.widget.ProgressBar>(R.id.progressBarComments)

        val commentAdapter = CommentAdapter()
        rvComments.adapter = commentAdapter

        progressBar.visibility = View.VISIBLE
        viewModel.loadCommentsForPost(postId)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .show()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.comments.collect { commentsList ->
                    progressBar.visibility = View.GONE
                    commentAdapter.submitList(commentsList)

                    if (commentsList.isNotEmpty()) {
                        rvComments.scrollToPosition(0)
                    }
                }
            }
        }

        btnSendComment.setOnClickListener {
            val content = etNewComment.text.toString().trim()
            if (content.isNotEmpty()) {
                viewModel.submitComment(postId, content)

                etNewComment.text.clear()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}