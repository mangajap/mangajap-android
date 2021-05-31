package com.tanasi.mangajap.adapters.viewHolders

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.tanasi.mangajap.R
import com.tanasi.mangajap.activities.MainActivity
import com.tanasi.mangajap.adapters.SpinnerAdapter
import com.tanasi.mangajap.databinding.*
import com.tanasi.mangajap.fragments.discover.DiscoverFragment
import com.tanasi.mangajap.fragments.discover.DiscoverFragmentDirections
import com.tanasi.mangajap.fragments.manga.MangaFragment
import com.tanasi.mangajap.fragments.manga.MangaFragmentDirections
import com.tanasi.mangajap.fragments.reviews.ReviewsFragment
import com.tanasi.mangajap.fragments.search.SearchFragment
import com.tanasi.mangajap.fragments.search.SearchFragmentDirections
import com.tanasi.mangajap.models.Manga
import com.tanasi.mangajap.models.MangaEntry
import com.tanasi.mangajap.models.Request
import com.tanasi.mangajap.models.User
import com.tanasi.mangajap.ui.dialog.EditTextDialog
import com.tanasi.mangajap.ui.dialog.MediaEntryDateDialog
import com.tanasi.mangajap.ui.dialog.MediaEntryProgressionDialog
import com.tanasi.mangajap.ui.dialog.NumberPickerDialog
import com.tanasi.mangajap.utils.extensions.*
import com.tanasi.mangajap.utils.preferences.UserPreference
import java.text.DecimalFormat
import java.util.*

class VhManga(
        private val _binding: ViewBinding
) : RecyclerView.ViewHolder(
        _binding.root
) {

    private val context: Context = itemView.context

    private lateinit var manga: Manga

    fun setVhManga(manga: Manga) {
        this.manga = manga
        when (_binding) {
            is ItemMediaSearchBinding -> displaySearch(_binding)
            is ItemMediaSearchAddBinding -> displaySearchAdd(_binding)
            is ItemMediaTrendingBinding -> displayTrending(_binding)

            is ItemMangaHeaderBinding -> displayHeader(_binding)
            is ItemMangaSummaryBinding -> displaySummary(_binding)
            is ItemMangaProgressionBinding -> displayProgression(_binding)
            is ItemMangaReviewsBinding -> displayReviews(_binding)
        }
    }


    private fun createMangaEntry(mangaEntry: MangaEntry) {
        if (context is MainActivity) {
            when (val fragment = context.getCurrentFragment()) {
                is SearchFragment -> fragment.viewModel.createMangaEntry(manga, mangaEntry)
                is DiscoverFragment -> fragment.viewModel.createMangaEntry(manga, mangaEntry)
            }
        }
    }

    private fun updateMangaEntry(mangaEntry: MangaEntry) {
        if (context is MainActivity) {
            when (val fragment = context.getCurrentFragment()) {
                is MangaFragment -> fragment.viewModel.updateMangaEntry(mangaEntry)
                is SearchFragment -> fragment.viewModel.updateMangaEntry(manga, mangaEntry)
                is DiscoverFragment -> fragment.viewModel.updateMangaEntry(manga, mangaEntry)
            }
        }
    }

    private fun createMangaRequest(request: Request) {
        if (context is MainActivity) {
            when (val fragment = context.getCurrentFragment()) {
                is SearchFragment -> fragment.viewModel.createRequest(request)
            }
        }
    }

    private fun displaySearch(binding: ItemMediaSearchBinding) {
        binding.media.setOnClickListener {
            Navigation.findNavController(binding.root).navigate(
                    SearchFragmentDirections.actionSearchToManga(
                            manga.id,
                            manga.canonicalTitle
                    )
            )
        }

        binding.mediaCoverImageView.apply {
            Picasso.get()
                    .load(manga.coverImage)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(this)
        }

        binding.mediaTitleTextView.text = manga.canonicalTitle

        binding.mediaTypeTextView.text = manga.mangaType?.stringId?.let { context.resources.getString(it) }

        binding.mediaMembersTextView.text = context.resources.getString(R.string.userCount, manga.userCount)

        binding.mediaIsAddCheckBox.apply {
            isChecked = manga.mangaEntry?.isAdd ?: false
            setOnClickListener {
                manga.mangaEntry?.let {
                    it.putAdd(isChecked)
                    updateMangaEntry(it)
                } ?: MangaEntry().also {
                    it.putAdd(isChecked)
                    it.putStatus(MangaEntry.Status.reading)
                    it.putUser(User().apply { id = UserPreference(context).selfId })
                    it.putManga(manga)
                    createMangaEntry(it)
                }
            }
        }
    }

    private fun displaySearchAdd(binding: ItemMediaSearchAddBinding) {
        binding.media.setOnClickListener {
            var query = ""
            if (context is MainActivity && context.getCurrentFragment() is SearchFragment) {
                when (val fragment = context.getCurrentFragment()) {
                    is SearchFragment -> query = fragment.query
                }
            }

            EditTextDialog(
                    context,
                    context.getString(R.string.propose_manga),
                    context.getString(R.string.manga_title),
                    query
            ) { dialog, _, text ->
                createMangaRequest(Request().also {
                    it.putRequestType(Request.RequestType.manga)
                    it.putData(text)
                    it.putUser(User().apply { id = UserPreference(context).selfId })
                })
                dialog.dismiss()
            }.show()
        }

        binding.tvMediaTitle.text = context.getString(R.string.propose_manga)

        binding.tvMediaSubtitle.text = context.getString(R.string.propose_manga_summary)
    }

    private fun displayTrending(binding: ItemMediaTrendingBinding) {
        binding.media.setOnClickListener {
            Navigation.findNavController(binding.root).navigate(
                    DiscoverFragmentDirections.actionDiscoverToManga(
                            manga.id,
                            manga.canonicalTitle
                    )
            )
        }

        binding.mediaCoverImageView.apply {
            Picasso.get()
                    .load(manga.coverImage)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(this)
        }

        binding.mediaIsAddCheckBox.apply {
            isChecked = manga.mangaEntry?.isAdd ?: false
            visibility = if (manga.mangaEntry?.isAdd == true) View.GONE else View.VISIBLE
            setOnClickListener {
                manga.mangaEntry?.let {
                    it.putAdd(isChecked)
                    updateMangaEntry(it)
                } ?: MangaEntry().also {
                    it.putAdd(isChecked)
                    it.putStatus(MangaEntry.Status.reading)
                    it.putUser(User().apply { id = UserPreference(context).selfId })
                    it.putManga(manga)
                    createMangaEntry(it)
                }
            }
        }

        binding.mediaProgressProgressBar.apply {
            manga.mangaEntry?.let { mangaEntry ->
                visibility = View.VISIBLE
                progress = mangaEntry.getProgress(manga)
                progressTintList = ContextCompat.getColorStateList(context, mangaEntry.getProgressColor(manga))
            } ?: let {
                visibility = View.GONE
            }
        }
    }

    private fun displayHeader(binding: ItemMangaHeaderBinding) {
        binding.mangaBannerImageView.apply {
            Picasso.get()
                    .load(manga.bannerImage ?: manga.coverImage)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(this)
            setOnClickListener {
                Navigation.findNavController(binding.root).navigate(
                        MangaFragmentDirections.actionMangaToImage(
                                manga.bannerImage ?: manga.coverImage ?: ""
                        )
                )
            }
        }

        binding.mangaCoverImageView.apply {
            Picasso.get().load(manga.coverImage).into(this)
            setOnClickListener {
                Navigation.findNavController(binding.root).navigate(
                        MangaFragmentDirections.actionMangaToImage(
                                manga.coverImage ?: ""
                        )
                )
            }
        }

        binding.mangaTitleTextView.text = manga.canonicalTitle

        binding.mangaStaff.apply {
            if (manga.staff.isEmpty()) {
                visibility = View.GONE
            } else {
                if (childCount > 0) {
                    removeAllViews()
                }
                for (staff in manga.staff) {
                    TextView(context).let { textView ->
                        staff.people?.let { people ->
                            val peopleName = when (people.pseudo) {
                                "" -> "${people.firstName} ${people.lastName}"
                                else -> people.pseudo
                            }
                            textView.text = peopleName
                            textView.setTextColor(context.getAttrColor(R.attr.textSecondaryColor))
                            textView.typeface = Typeface.DEFAULT_BOLD
                            textView.setOnClickListener {
                                Navigation.findNavController(binding.root).navigate(
                                    MangaFragmentDirections.actionMangaToPeople(
                                        people.id,
                                        peopleName
                                    )
                                )
                            }
                        }
                        addView(textView)
                    }
                }
                visibility = View.VISIBLE
            }
        }

        binding.mangaTypeTextView.text = context.getString(manga.mangaType?.stringId ?: Manga.MangaType.shonen.stringId)
    }

    private fun displaySummary(binding: ItemMangaSummaryBinding) {
        binding.mangaSummarySubtitleTextView.apply {
            text = context.getString(R.string.manga_metadata,
                    manga.startDate?.format("yyyy"),
                    manga.endDate?.format("yyyy") ?: context.getString(manga.status.stringId),
                    manga.origin?.getDisplayCountry(context.locale()))
        }

        binding.mangaScoreTextView.text = manga.averageRating?.let { DecimalFormat("#.#").format(it / 20.0 * 5) + " / 5" } ?: "- / 5"

        binding.mangaSynopsisTextView.apply {
            text = manga.synopsis
            maxLines = MangaFragment.MANGA_SYNOPSIS_MAX_LINES
        }

        binding.mangaReadMore.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                binding.mangaSynopsisTextView.maxLines = Int.MAX_VALUE
                visibility = View.GONE
            }
        }

        binding.mangaRankedTextView.text = context.getString(R.string.manga_rating, (manga.ratingRank ?: ""))

        binding.mangaVolumeCountTextView.text = when (manga.status) {
            Manga.Status.publishing -> context.resources.getString(R.string.approximatelyVolumeCount, manga.volumeCount)
            else -> context.resources.getString(R.string.volumeCount, manga.volumeCount)
        }

        binding.mangaChapterCountTextView.text = when (manga.status) {
            Manga.Status.publishing -> context.resources.getString(R.string.approximatelyChapterCount, manga.chapterCount)
            else -> context.resources.getString(R.string.chapterCount, manga.chapterCount)
        }

        binding.mangaUserCountTextView.text = context.resources.getString(R.string.mangaUserCount, manga.userCount)
    }

    private fun displayProgression(binding: ItemMangaProgressionBinding) {
        binding.spinnerMangaEntryStatus.apply {
            (background as GradientDrawable).setStroke(context.dpToPx(1), ContextCompat.getColor(context, manga.mangaEntry?.getProgressColor(manga) ?: MangaEntry.Status.reading.colorId))

            adapter = SpinnerAdapter(
                context,
                MangaEntry.Status.values().asList(),
            ).apply {
                onView = { position, context, parent ->
                    ItemSpinnerMediaStatusBinding.inflate(LayoutInflater.from(context), parent, false).also {
                        it.root.text = context.getString(MangaEntry.Status.values()[position].stringId)
                        it.root.setTextColor(ContextCompat.getColor(context, manga.mangaEntry?.getProgressColor(manga) ?: MangaEntry.Status.reading.colorId))
                    }.root
                }
                onBind = { position, context, parent ->
                    ItemSpinnerDropdownMediaStatusBinding.inflate(LayoutInflater.from(context), parent, false).also {
                        it.root.text = context.getString(MangaEntry.Status.values()[position].stringId)
                        it.root.setTextColor(ContextCompat.getColor(context, MangaEntry.Status.values()[position].colorId))
                    }.root
                }
            }

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                    manga.mangaEntry?.let {
                        if (MangaEntry.Status.values()[position] != it.status) {
                            it.putStatus(MangaEntry.Status.values()[position])
                            when (MangaEntry.Status.values()[position]) {
                                MangaEntry.Status.reading -> if (it.startedAt == null) it.putStartedAt(Calendar.getInstance())
                                MangaEntry.Status.completed,
                                MangaEntry.Status.on_hold,
                                MangaEntry.Status.dropped -> if (it.finishedAt == null) it.putFinishedAt(Calendar.getInstance())
                                else -> {}
                            }
                            updateMangaEntry(it)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            setSelection(manga.mangaEntry?.status?.ordinal ?: 0)
        }

        binding.mangaMyDateTextView.apply {
            val startedAt = manga.mangaEntry?.startedAt?.format("dd MMMM yyyy") ?: "-"
            val finishedAt = manga.mangaEntry?.finishedAt?.format("dd MMMM yyyy") ?: "-"

            visibility = when (manga.mangaEntry?.status) {
                MangaEntry.Status.planned -> View.GONE
                else -> View.VISIBLE
            }
            text = when (manga.mangaEntry?.status) {
                MangaEntry.Status.reading -> context.resources.getString(R.string.SinceThe, startedAt)
                MangaEntry.Status.completed -> context.resources.getString(R.string.CompletedSinceThe, finishedAt)
                MangaEntry.Status.on_hold -> context.resources.getString(R.string.OnHoldSinceThe, finishedAt)
                MangaEntry.Status.dropped -> context.resources.getString(R.string.DroppedSinceThe, finishedAt)
                else -> ""
            }
            setOnClickListener {
                MediaEntryDateDialog(
                        context,
                        context.getString(R.string.progression),
                        manga.mangaEntry?.startedAt,
                        manga.mangaEntry?.finishedAt,
                ) { startedAt, finishedAt ->
                    manga.mangaEntry?.let {
                        it.putStartedAt(startedAt)
                        it.putFinishedAt(finishedAt)
                        updateMangaEntry(it)
                    }
                }.show()
            }
        }

        binding.mangaVolumesRead.setOnClickListener {
            MediaEntryProgressionDialog(
                    context,
                    context.getString(R.string.volumesRead),
                    manga.mangaEntry?.volumesRead ?: 0
            ) { value ->

                manga.mangaEntry?.let {
                    it.putVolumesRead(value)
                    updateMangaEntry(it)
                }
            }.show()
        }

        binding.mangaVolumesReadTextView.text = manga.mangaEntry?.volumesRead?.toString() ?: "0"

        binding.mangaVolumesTextView.text = context.resources.getString(R.string.VolumesRead, manga.volumeCount
                ?: 0)

        binding.mangaChaptersRead.setOnClickListener {
            MediaEntryProgressionDialog(
                    context,
                    context.getString(R.string.chaptersRead),
                    manga.mangaEntry?.chaptersRead ?: 0
            ) { value ->
                manga.mangaEntry?.let {
                    it.putChaptersRead(value)
                    updateMangaEntry(it)
                }
            }.show()
        }

        binding.mangaChaptersReadTextView.text = manga.mangaEntry?.chaptersRead?.toString() ?: "0"

        binding.mangaChaptersTextView.text = context.resources.getString(R.string.ChaptersRead, manga.chapterCount
                ?: 0)

        binding.mangaEntryRatingTextView.apply {
            text = "${manga.mangaEntry?.rating ?: "-"} / 20"
            setOnClickListener {
                NumberPickerDialog(
                        context,
                        context.getString(R.string.score),
                        0,
                        20,
                        manga.mangaEntry?.rating
                ) { value ->
                    manga.mangaEntry?.let {
                        it.putRating(value)
                        updateMangaEntry(it)
                    }
                }.show()
            }
        }

        binding.mangaEntryRemoveRatingImageView.setOnClickListener {
            manga.mangaEntry?.let {
                it.putRating(null)
                updateMangaEntry(it)
            }
        }

        binding.mangaEntryIsFavoritesImageView.apply {
            if (manga.mangaEntry?.isFavorites == true) setImageResource(R.drawable.ic_favorite_black_24dp)
            else setImageResource(R.drawable.ic_favorite_border_black_24dp)
            setOnClickListener {
                manga.mangaEntry?.let {
                    it.putFavorites(!it.isFavorites)
                    updateMangaEntry(it)
                }
            }
        }
    }

    private fun displayReviews(binding: ItemMangaReviewsBinding) {
        binding.llMangaAllReviews.setOnClickListener {
            Navigation.findNavController(binding.root).navigate(
                    MangaFragmentDirections.actionMangaToReviews(
                            ReviewsFragment.ReviewsType.Manga,
                            manga.id,
                            manga.canonicalTitle
                    )
            )
        }

        binding.tvMangaReviewCount.text = manga.reviewCount.toString()
    }
}