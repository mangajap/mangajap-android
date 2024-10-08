package com.tanasi.mangajap.fragments.profileedit

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.tanasi.jsonapi.JsonApiResponse
import com.tanasi.mangajap.R
import com.tanasi.mangajap.adapters.SpinnerAdapter
import com.tanasi.mangajap.databinding.FragmentProfileEditBinding
import com.tanasi.mangajap.models.User
import com.tanasi.mangajap.utils.extensions.format
import com.tanasi.mangajap.utils.extensions.getCountries
import com.tanasi.mangajap.utils.extensions.isStoragePermissionGranted
import com.tanasi.mangajap.utils.extensions.setToolbar
import com.tanasi.mangajap.utils.extensions.toBase64
import kotlinx.coroutines.launch
import java.util.Calendar


class ProfileEditFragment : Fragment() {

    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<ProfileEditViewModel>()

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permission ->
        if (permission) {
            Toast.makeText(
                requireContext(),
                getString(R.string.permissionGranted),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val bitmap = when {
                    Build.VERSION.SDK_INT >= 29 -> ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            requireActivity().contentResolver,
                            uri
                        )
                    )

                    else -> {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(
                            requireActivity().contentResolver,
                            uri
                        )
                    }
                }
                binding.civProfileEditUserProfilePic.setImageBitmap(bitmap)
                user.avatar = User.Avatar(
                    "", "", "", "",
                    original = bitmap.toBase64()
                )
            }
        }
    }

    private lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeProfileEdit()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { state ->
                when (state) {
                    ProfileEditViewModel.State.Loading -> binding.isLoading.apply {
                        root.visibility = View.VISIBLE
                    }

                    is ProfileEditViewModel.State.SuccessLoading -> {
                        user = state.user
                        displayProfile(state.user)
                        binding.isLoading.root.visibility = View.GONE
                    }

                    is ProfileEditViewModel.State.FailedLoading -> {
                        when (state.error) {
                            is JsonApiResponse.Error.ServerError -> state.error.body.errors.map {
                                Toast.makeText(requireContext(), it.title, Toast.LENGTH_SHORT)
                                    .show()
                            }

                            is JsonApiResponse.Error.NetworkError -> Toast.makeText(
                                requireContext(),
                                state.error.error.message ?: "",
                                Toast.LENGTH_SHORT
                            ).show()

                            is JsonApiResponse.Error.UnknownError -> Toast.makeText(
                                requireContext(),
                                state.error.error.message ?: "",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    ProfileEditViewModel.State.Updating -> binding.isUpdating.apply {
                        root.visibility = View.VISIBLE
                    }

                    is ProfileEditViewModel.State.SuccessUpdating -> {
                        findNavController().navigateUp()
                    }

                    is ProfileEditViewModel.State.FailedUpdating -> {
                        when (state.error) {
                            is JsonApiResponse.Error.ServerError -> state.error.body.errors.map {
                                Toast.makeText(requireContext(), it.title, Toast.LENGTH_SHORT)
                                    .show()
                            }

                            is JsonApiResponse.Error.NetworkError -> Toast.makeText(
                                requireContext(),
                                state.error.error.message ?: "",
                                Toast.LENGTH_SHORT
                            ).show()

                            is JsonApiResponse.Error.UnknownError -> Toast.makeText(
                                requireContext(),
                                state.error.error.message ?: "",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (this::user.isInitialized) {
            inflater.inflate(R.menu.menu_fragment_profile_edit, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                if (this::user.isInitialized) {
                    viewModel.updateUser(user)
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun initializeProfileEdit() {
        setToolbar(getString(R.string.editProfile), "")
        setHasOptionsMenu(true)
    }

    private fun displayProfile(user: User) {
        requireActivity().invalidateOptionsMenu()

        binding.trProfileEditUserProfilePic.setOnClickListener {
            if (requireContext().isStoragePermissionGranted()) {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                pickImage.launch(intent)
            } else {
                requestPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        binding.civProfileEditUserProfilePic.apply {
            Picasso.get()
                .load(user.avatar?.small)
                .placeholder(R.drawable.default_user_avatar)
                .error(R.drawable.default_user_avatar)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(this)
        }

        binding.ivProfileEditDeleteUserProfilePic.setOnClickListener {
            binding.civProfileEditUserProfilePic.setImageResource(R.drawable.default_user_avatar)
            user.avatar = null
        }


        binding.etProfileEditUserAbout.apply {
            append(user.about)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    user.about = s.toString()
                }
            })
        }

        binding.etProfileEditUserFirstName.apply {
            append(user.firstName)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    user.firstName = s.toString()
                }
            })
        }

        binding.etProfileEditUserLastName.apply {
            append(user.lastName)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    user.lastName = s.toString()
                }
            })
        }

        binding.tvProfileEditUserBirthday.apply {
            text = user.birthday?.format("dd MMMM yyyy") ?: ""
            setOnClickListener {
                (user.birthday ?: Calendar.getInstance()).let {
                    DatePickerDialog(
                        requireContext(),
                        { _, year, month, dayOfMonth ->
                            val date = Calendar.getInstance()
                            date[year, month] = dayOfMonth

                            user.birthday = date
                            text = date.format("dd MMMM yyyy")
                        },
                        it[Calendar.YEAR],
                        it[Calendar.MONTH],
                        it[Calendar.DAY_OF_MONTH]
                    ).show()
                }
            }
        }

        binding.ivProfileEditDeleteUserBirthday.setOnClickListener {
            user.birthday = null
            binding.tvProfileEditUserBirthday.text = ""
        }

        binding.spinnerProfileEditUserGender.apply {
            adapter = SpinnerAdapter(
                context,
                User.Gender.entries
                    .map { getString(it.stringId) }
                    .toMutableList()
                    .also {
                        it.add(0, "------")
                    }
            )
            setSelection(user.gender?.ordinal?.let { it + 1 } ?: 0)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    user.gender = when (position) {
                        0 -> null
                        else -> User.Gender.entries[position - 1]
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        binding.spinnerProfileEditUserCountry.apply {
            val countries: MutableList<String> = mutableListOf()
            val countriesCode: MutableList<String> = mutableListOf()

            countries.add(context.resources.getString(R.string.none))
            countriesCode.add("")
            requireContext().getCountries()
                .toList()
                .sortedBy { (_, country) ->
                    country.lowercase()
                }
                .map { (code, country) ->
                    countries.add(country)
                    countriesCode.add(code)
                }

            adapter = SpinnerAdapter(context, countries)
            setSelection(countriesCode.indexOf(user.country))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    user.country = countriesCode[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }
}