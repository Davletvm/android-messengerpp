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

package org.solovyev.android.messenger;

import com.google.common.base.Predicate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solovyev.android.db.Dao;
import org.solovyev.android.messenger.entities.EntityAware;
import org.solovyev.common.Objects;
import org.solovyev.common.equals.CollectionEqualizer;
import org.solovyev.common.equals.Equalizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.find;
import static org.junit.Assert.*;

public abstract class DefaultDaoTest<E> extends DefaultMessengerTest {

	@Nullable
	private final Equalizer<E> equalsEqualizer;

	@Nullable
	private final Equalizer<E> sameEqualizer;

	@Nonnull
	private Dao<E> dao;

	protected DefaultDaoTest(@Nullable Equalizer<E> equalsEqualizer, @Nullable Equalizer<E> sameEqualizer) {
		this.equalsEqualizer = equalsEqualizer;
		this.sameEqualizer = sameEqualizer;
	}

	protected DefaultDaoTest(@Nullable Equalizer<E> sameEqualizer) {
		this(null, sameEqualizer);
	}

	protected DefaultDaoTest() {
		this(null, null);
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		dao = getDao();
	}

	@Nonnull
	protected abstract Dao<E> getDao();

	@Test
	public void testShouldInsertEntity() throws Exception {
		final E entity = insertEntity().entity;
		assertTrue(any(dao.readAll(), new SamePredicate(entity)));
	}

	@Nonnull
	private DaoEntity<? extends E> insertEntity() {
		final DaoEntity<? extends E> entity = newInsertEntity();
		dao.create(entity.entity);
		return entity;
	}

	@Test
	public void testShouldDeleteExistingEntity() throws Exception {
		// create entity
		final E entity = insertEntity().entity;
		try {
			dao.delete(entity);
			assertFalse(any(dao.readAll(), new EqualsPredicate(entity)));
		} catch (UnsupportedOperationException e) {
			onUnsupportedOperationException(e);
		}
	}

	private void onUnsupportedOperationException(@Nonnull UnsupportedOperationException e) {
		System.out.println("Delete test skipped due to unsupported operation exception: ");
		e.printStackTrace(System.out);
	}

	@Test
	public void testDeleteShouldNotAffectOtherEntities() throws Exception {
		final Collection<E> before = dao.readAll();
		final E entity = insertEntity().entity;
		try {
			dao.delete(entity);
			final Collection<E> after = dao.readAll();
			assertEntitiesSame(before, after);
		} catch (UnsupportedOperationException e) {
			onUnsupportedOperationException(e);
		}
	}

	@Test
	public void testReadAllShouldLoadAllEntities() throws Exception {
		final Collection<E> entities = populateEntities(dao);
		assertEntitiesSame(entities, dao.readAll());
	}

	@Test
	public void testShouldLoadAllIds() throws Exception {
		final Collection<E> entities = populateEntities(dao);
		final Collection<String> ids = dao.readAllIds();

		final Set<String> idSet = new HashSet<String>(ids);
		assertEquals(ids.size(), idSet.size());

		for (final String id : ids) {
			find(entities, new Predicate<E>() {
				@Override
				public boolean apply(@Nullable E e) {
					return getId(e).equals(id);
				}
			});
		}
	}

	@Nonnull
	protected abstract String getId(E entity);

	@Test
	public void testShouldReadEntityById() throws Exception {
		final DaoEntity<? extends E> entity = insertEntity();
		assertEntitiesSame(entity.entity, dao.read(entity.id));
	}

	@Test
	public void testShouldUpdateEntity() throws Exception {
		final DaoEntity<? extends E> e1 = insertEntity();
		final DaoEntity<E> e2 = newEntity(changeEntity(e1.entity), e1.id);
		dao.update(e2.entity);
		assertTrue(any(dao.readAll(), new SamePredicate(e2.entity)));
	}

	@Nonnull
	protected abstract Collection<E> populateEntities(@Nonnull Dao<E> dao);

	@Nonnull
	protected abstract DaoEntity<? extends E> newInsertEntity();

	@Nonnull
	protected abstract E changeEntity(@Nonnull E entity);

	@Nonnull
	protected static <E extends EntityAware> DaoEntity<E> newEntity(@Nonnull E entity) {
		return new DaoEntity<E>(entity, entity.getEntity().getEntityId());
	}

	@Nonnull
	protected static <E> DaoEntity<E> newEntity(@Nonnull E entity, @Nonnull String id) {
		return new DaoEntity<E>(entity, id);
	}

	/*
	**********************************************************************
	*
	*                           COMPARISON
	*
	**********************************************************************
	*/

	protected final void assertEntitiesSame(@Nonnull Collection<E> c1, @Nonnull Collection<E> c2) {
		Assert.assertTrue(Objects.areEqual(c1, c2, new CollectionEqualizer<E>(sameEqualizer)));
	}

	protected final void assertEntitiesEqual(@Nonnull Collection<E> c1, @Nonnull Collection<E> c2) {
		Assert.assertTrue(Objects.areEqual(c1, c2, new CollectionEqualizer<E>(equalsEqualizer)));
	}

	protected final void assertEntitiesSame(@Nonnull E e1, @Nonnull E e2) {
		assertTrue(areSame(e1, e2));
	}

	protected final void assertEntitiesEqual(@Nonnull E e1, @Nonnull E e2) {
		assertTrue(areEqual(e1, e2));
	}

	protected final boolean areSame(@Nonnull E e1, @Nonnull E e2) {
		return Objects.areEqual(e1, e2, sameEqualizer);
	}

	protected final boolean areEqual(@Nonnull E e1, @Nonnull E e2) {
		return Objects.areEqual(e1, e2, equalsEqualizer);
	}

	/*
	**********************************************************************
	*
	*                           STATIC/INNER
	*
	**********************************************************************
	*/

	protected static final class DaoEntity<E> {
		@Nonnull
		private final E entity;

		@Nonnull
		private final String id;

		private DaoEntity(@Nonnull E entity, @Nonnull String id) {
			this.entity = entity;
			this.id = id;
		}
	}

	private class SamePredicate implements Predicate<E> {

		@Nonnull
		private final E entity;

		public SamePredicate(@Nonnull E entity) {
			this.entity = entity;
		}

		@Override
		public boolean apply(@Nullable E e) {
			return e != null && areSame(e, entity);
		}
	}

	private class EqualsPredicate implements Predicate<E> {

		@Nonnull
		private final E entity;

		public EqualsPredicate(@Nonnull E entity) {
			this.entity = entity;
		}

		@Override
		public boolean apply(@Nullable E e) {
			return e != null && areEqual(e, entity);
		}
	}
}
