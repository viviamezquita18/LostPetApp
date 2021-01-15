package com.app.lostpetapp.ui

import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.view.doOnPreDraw
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.app.lostpetapp.R
import com.app.lostpetapp.databinding.FragmentPetListBinding
import com.app.lostpetapp.model.Pet
import com.app.lostpetapp.ui.adapters.PetAdapter
import com.app.lostpetapp.ui.viewModels.PetListViewModel
import com.app.lostpetapp.util.ItemDecoration
import com.app.lostpetapp.util.NetworkApiStatus
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class PetListFragment : Fragment() {

    private val viewModel: PetListViewModel by viewModels()
    private lateinit var binding: FragmentPetListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pet_list, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        val view = binding.root
        setRecyclerView()
        subscribeToStatusObserver()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    private fun setRecyclerView() {
        binding.petList.itemAnimator = null;
        binding.petList.addItemDecoration(
            ItemDecoration(
                resources.getDimension(R.dimen.margin_s).toInt()
            )
        )
        val adapter = PetAdapter()
        binding.petList.adapter = adapter
        adapter.onItemClickListener = object : PetAdapter.OnItemClickListener {

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onClick(pet: Pet, view: ImageView) {
                val action = PetListFragmentDirections.actionPetListFragmentToDetailPetFragment(
                    pet,
                    view.transitionName
                )
                val imageTransitionName = view.transitionName;
                val extras = FragmentNavigatorExtras(view to imageTransitionName)
                postponeEnterTransition()
                view.doOnPreDraw { startPostponedEnterTransition() }
                findNavController().navigate(
                    action, extras
                )
            }
        }
    }

    private fun subscribeToStatusObserver() {
        viewModel.status.observe(requireActivity(), Observer {
            when (it) {
                NetworkApiStatus.NETWORK_ERROR -> {
                    binding.statusImage.text = getString(R.string.no_internet_connection)
                    binding.statusImage.visibility = View.VISIBLE
                    binding.loadingPanel.visibility = View.GONE
                }
                NetworkApiStatus.ERROR -> {
                    binding.statusImage.text =
                        resources.getString(R.string.no_server_connection)
                    binding.statusImage.visibility = View.VISIBLE
                    binding.loadingPanel.visibility = View.GONE
                }
                NetworkApiStatus.DONE -> {
                    binding.loadingPanel.visibility = View.GONE
                }
            }
        })
    }


}