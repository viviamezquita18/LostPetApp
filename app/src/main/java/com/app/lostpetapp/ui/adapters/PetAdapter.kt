package com.app.lostpetapp.ui.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.app.lostpetapp.databinding.PetCardLayoutBinding
import com.app.lostpetapp.model.Pet


class PetAdapter() :
    RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null
    private var petList = emptyList<Pet>()

    fun setData(pet: List<Pet>) {
        petList = pet
        notifyDataSetChanged()
    }

    class PetViewHolder(private var binding: PetCardLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val cardView = binding.petImage
        fun bind(pet: Pet) {
            binding.pet = pet
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PetViewHolder {
        return PetViewHolder(PetCardLayoutBinding.inflate(LayoutInflater.from(parent.context)))
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {

        val pet = petList.get(position)
        holder.cardView.setOnClickListener {
            onItemClickListener?.onClick(pet, holder.cardView)
        }
        holder.bind(pet)
        holder.cardView.transitionName = "trans_image$position"
    }

    override fun getItemCount(): Int {
        return petList.size
    }

    interface OnItemClickListener {
        fun onClick(pet: Pet, view: ImageView)
    }

}


