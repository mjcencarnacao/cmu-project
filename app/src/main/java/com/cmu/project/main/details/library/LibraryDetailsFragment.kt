package com.cmu.project.main.details.library

import androidx.fragment.app.Fragment
import com.cmu.project.R
import com.cmu.project.databinding.FragmentLibraryDetailsBinding

class LibraryDetailsFragment : Fragment(R.layout.fragment_library_details), LibraryDetailsContract.View {

    override lateinit var presenter: LibraryDetailsPresenter
    private lateinit var binding: FragmentLibraryDetailsBinding

}