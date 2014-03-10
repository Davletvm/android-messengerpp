/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.list;

import android.database.DataSetObserver;
import android.util.SparseIntArray;
import android.widget.SectionIndexer;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.StringComparator.compareStrings;

/**
 * A helper class for adapters that implement the SectionIndexer interface.
 * If the items in the adapter are sorted by simple alphabet-based sorting, then
 * this class provides a way to do fast indexing of large lists using binary search.
 * It caches the indices that have been determined through the binary search and also
 * invalidates the cache if changes occur in the cursor.
 * <p/>
 * Your adapter is responsible for updating the cursor by calling {@link #setCursor} if the
 * cursor changes. {@link #getPositionForSection} method does the binary search for the starting
 * index of a given section (alphabet).
 */

/**
 * User: serso
 * Date: 5/29/13
 * Time: 10:01 PM
 */
public final class AlphabetIndexer extends DataSetObserver implements SectionIndexer {

	@Nonnull
	private Alphabet alphabet;

	@Nonnull
	private final ListAdapter<?> adapter;

	/**
	 * This contains a cache of the computed indices so far. It will get reset whenever
	 * the dataset changes or the cursor changes.
	 */
	private final SparseIntArray alphabetMap;

	/**
	 * Constructs the indexer.
	 *
	 * @param alphabet string containing the alphabet, with space as the first character.
	 *                 For example, use the string " ABCDEFGHIJKLMNOPQRSTUVWXYZ" for English indexing.
	 *                 The characters must be uppercase and be sorted in ascii/unicode order. Basically
	 *                 characters in the alphabet will show up as preview letters.
	 */
	private AlphabetIndexer(@Nonnull Alphabet alphabet, @Nonnull ListAdapter<?> adapter) {
		this.adapter = adapter;
		this.alphabet = alphabet;
		alphabetMap = new SparseIntArray(this.alphabet.getLength());
	}

	@Nonnull
	public static AlphabetIndexer createAndAttach(@Nonnull Alphabet alphabet, @Nonnull ListAdapter<?> adapter) {
		final AlphabetIndexer result = new AlphabetIndexer(alphabet, adapter);
		adapter.registerDataSetObserver(result);
		return result;
	}

	@Nonnull
	public static AlphabetIndexer createAndAttach(@Nonnull ListAdapter<?> adapter) {
		final AlphabetIndexer result = new AlphabetIndexer(Alphabet.forCharacters(extractAlphabet(adapter)), adapter);
		adapter.registerDataSetObserver(result);
		return result;
	}

	@Nonnull
	private static CharSequence extractAlphabet(@Nonnull ListAdapter<?> adapter) {
		final StringBuilder alphabet = new StringBuilder(50);
		for (Object element : adapter.getAllElements()) {
			final String s = element.toString();
			if (s.length() > 0) {
				final char newChar = s.charAt(0);
				if (alphabet.length() > 0) {
					final char lastChar = alphabet.charAt(alphabet.length() - 1);
					if (lastChar != newChar) {
						if (Character.toUpperCase(lastChar) != Character.toUpperCase(newChar)) {
							alphabet.append(newChar);
						}
					}
				} else {
					alphabet.append(newChar);
				}
			}
		}
		return alphabet;
	}

	/**
	 * Returns the section array constructed from the alphabet provided in the constructor.
	 *
	 * @return the section array
	 */
	public Object[] getSections() {
		return alphabet.getLetters();
	}

	/**
	 * Default implementation compares the first character of word with letter.
	 */
	protected int compare(String word, String letter) {
		final String firstLetter;
		if (word.length() == 0) {
			firstLetter = " ";
		} else {
			firstLetter = word.substring(0, 1);
		}

		return compareStrings(firstLetter, letter);
	}

	/**
	 * Performs a binary search or cache lookup to find the first row that
	 * matches a given section's starting letter.
	 *
	 * @param sectionIndex the section to search for
	 * @return the row index of the first occurrence, or the nearest next letter.
	 * For instance, if searching for "T" and no "T" is found, then the first
	 * row starting with "U" or any higher letter is returned. If there is no
	 * data following "T" at all, then the list size is returned.
	 */
	public int getPositionForSection(int sectionIndex) {
		if (sectionIndex < 0) {
			return 0;
		}

		final SparseIntArray alphaMap = alphabetMap;

		if (alphabet == null) {
			return 0;
		}

		// Check bounds
		if (sectionIndex <= 0) {
			return 0;
		}
		if (sectionIndex >= alphabet.getLength()) {
			sectionIndex = alphabet.getLength() - 1;
		}

		final int count = adapter.getCount();
		int start = 0;
		int end = count;
		int position;

		final char letter = alphabet.getCharacterAt(sectionIndex);
		// Check map
		if (Integer.MIN_VALUE != (position = alphaMap.get(letter, Integer.MIN_VALUE))) {
			// Is it approximate? Using negative value to indicate that it's
			// an approximation and positive value when it is the accurate
			// position.
			if (position < 0) {
				position = -position;
				end = position;
			} else {
				// Not approximate, this is the confirmed start of section, return it
				return position;
			}
		}

		// Do we have the position of the previous section?
		if (sectionIndex > 0) {
			int prevLetter = alphabet.getCharacterAt(sectionIndex - 1);
			int prevLetterPos = alphaMap.get(prevLetter, Integer.MIN_VALUE);
			if (prevLetterPos != Integer.MIN_VALUE) {
				start = Math.abs(prevLetterPos);
			}
		}

		// Now that we have a possibly optimized start and end, let's binary search

		position = (end + start) / 2;

		final String l = Character.toString(letter);
		while (position < end) {
			// Get letter at pos
			final String itemName = adapter.getItem(position).toString();
			if (itemName == null) {
				if (position == 0) {
					break;
				} else {
					position--;
					continue;
				}
			}
			final int diff = compare(itemName, l);
			if (diff != 0) {
				// TODO: Commenting out approximation code because it doesn't work for certain
				// lists with custom comparators
				// Enter approximation in hash if a better solution doesn't exist
				// String startingLetter = Character.toString(getFirstLetter(curName));
				// int startingLetterKey = startingLetter.charAt(0);
				// int curPos = alphaMap.get(startingLetterKey, Integer.MIN_VALUE);
				// if (curPos == Integer.MIN_VALUE || Math.abs(curPos) > pos) {
				//     Negative pos indicates that it is an approximation
				//     alphaMap.put(startingLetterKey, -pos);
				// }
				// if (collator.compare(startingLetter, targetLetter) < 0) {
				if (diff < 0) {
					start = position + 1;
					if (start >= count) {
						position = count;
						break;
					}
				} else {
					end = position;
				}
			} else {
				// They're the same, but that doesn't mean it's the start
				if (start == position) {
					// This is it
					break;
				} else {
					// Need to go further lower to find the starting row
					end = position;
				}
			}
			position = (start + end) / 2;
		}
		alphaMap.put(letter, position);
		return position;
	}

	/**
	 * Returns the section index for a given position in the list by querying the item
	 * and comparing it with all items in the section array.
	 */
	public int getSectionForPosition(int position) {
		if (position < 0) {
			return 0;
		} else if (position >= adapter.getCount()) {
			return adapter.getCount() - 1;
		}

		String curName = adapter.getItem(position).toString();
		// Linear search, as there are only a few items in the section index
		// Could speed this up later if it actually gets used.
		for (int i = 0; i < alphabet.getLength(); i++) {
			char letter = alphabet.getCharacterAt(i);
			String targetLetter = Character.toString(letter);
			if (compare(curName, targetLetter) == 0) {
				return i;
			}
		}
		return 0; // Don't recognize the letter - falls under zero'th section
	}

	/*
	 * @hide
	 */
	@Override
	public void onChanged() {
		super.onChanged();
		updateAlphabet();
	}

	private void updateAlphabet() {
		alphabet = Alphabet.forCharacters(extractAlphabet(adapter));
		alphabetMap.clear();
	}

	/*
	 * @hide
	 */
	@Override
	public void onInvalidated() {
		super.onInvalidated();
		updateAlphabet();
	}
}

