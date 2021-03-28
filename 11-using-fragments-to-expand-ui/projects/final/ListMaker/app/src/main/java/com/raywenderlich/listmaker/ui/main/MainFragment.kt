package com.raywenderlich.listmaker.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.raywenderlich.listmaker.databinding.MainFragmentBinding
import com.raywenderlich.listmaker.models.TaskList

class MainFragment : Fragment(), ListSelectionRecyclerViewAdapter.ListSelectionRecyclerViewClickListener {

  interface MainFragmentInteractionListener {
    fun listItemTapped(list: TaskList)
  }

  lateinit var clickListener: MainFragmentInteractionListener

  private lateinit var binding: MainFragmentBinding

  companion object {
    fun newInstance() = MainFragment()
  }

  private lateinit var viewModel: MainViewModel
  


  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    binding = MainFragmentBinding.inflate(inflater, container, false)

    binding.listsRecyclerview.layoutManager = LinearLayoutManager(requireContext())

    return binding.root
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    viewModel = ViewModelProvider(
        requireActivity(),
        MainViewModelFactory(PreferenceManager.getDefaultSharedPreferences(requireActivity()))
    )
      .get(MainViewModel::class.java)

    val recyclerViewAdapter = ListSelectionRecyclerViewAdapter(viewModel.lists, this)

    binding.listsRecyclerview.adapter = recyclerViewAdapter

    viewModel.onListAdded = {
      recyclerViewAdapter.listsUpdated()
    }
  }

  override fun listItemClicked(list: TaskList) {
    clickListener.listItemTapped(list)
  }
}