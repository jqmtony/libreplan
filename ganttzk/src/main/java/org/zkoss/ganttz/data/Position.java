/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.zkoss.ganttz.data;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * Represents a position for a task <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class Position {

    /**
     * @param parents
     *            the list of ancestors from the parent to the more remote
     *            ancestor
     * @param positionInParent
     * @return a {@link Position} specified by the params
     */
    public static Position createPosition(
            List<? extends TaskContainer> parents, int positionInParent) {
        Validate.notEmpty(parents);
        Validate.noNullElements(parents);
        Validate.isTrue(positionInParent >= 0);
        Task firstParent = parents.get(0);
        Validate.isTrue(positionInParent < firstParent.getTasks().size());
        return new ChildPosition(parents, positionInParent);
    }

    public static Position createAtTopPosition(int position) {
        return new InsertInTopPosition(position);
    }

    public static Position createAppendToTopPosition() {
        return new AppendToTopPosition();
    }

    private final Integer position;

    protected Position(int position) {
        Validate.isTrue(position >= 0);
        this.position = position;
    }

    protected Position() {
        this.position = null;
    }

    public abstract boolean isAppendToTop();

    public abstract List<? extends TaskContainer> getAncestors();

    public abstract TaskContainer getParent();

    public boolean isAtTop() {
        return getAncestors().isEmpty();
    }

    public boolean specifiesPosition() {
        return position != null;
    }

    public Task getMostRemoteAncestor() {
        return getAncestors().get(getAncestors().size() - 1);
    }

    public Integer getInsertionPosition() {
        return position;
    }

    public abstract boolean canPop();

    /**
     * Removes the remotest ancestor
     * @return a new position
     * @throws UnsupportedOperationException
     *             if not {@link canPop}
     */
    public abstract Position pop() throws UnsupportedOperationException;


    private static class ChildPosition extends Position {

        private final List<? extends TaskContainer> parents;
        private TaskContainer parent;

        ChildPosition(List<? extends TaskContainer> parents,
                int positionInParent) {
            super(positionInParent);
            this.parents = parents;
            this.parent = parents.get(0);
        }

        @Override
        public boolean isAppendToTop() {
            return false;
        }

        @Override
        public TaskContainer getParent() {
            return parent;
        }

        @Override
        public List<? extends TaskContainer> getAncestors() {
            return parents;
        }

        @Override
        public Position pop() {
            return new ChildPosition(parents.subList(0, parents.size() - 1),
                    getInsertionPosition());
        }

        @Override
        public boolean canPop() {
            return parents.size() > 1;
        }
    }

    private static class AppendToTopPosition extends Position {
        @Override
        public boolean isAppendToTop() {
            return true;
        }

        @Override
        public TaskContainer getParent() {
            return null;
        }

        @Override
        public List<? extends TaskContainer> getAncestors() {
            return Arrays.asList();
        }

        @Override
        public Position pop() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean canPop() {
            return false;
        }
    }

    private static class InsertInTopPosition extends Position {

        InsertInTopPosition(int position) {
            super(position);
        }

        @Override
        public boolean isAppendToTop() {
            return false;
        }

        @Override
        public TaskContainer getParent() {
            return null;
        }

        @Override
        public List<? extends TaskContainer> getAncestors() {
            return Arrays.asList();
        }

        @Override
        public Position pop() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean canPop() {
            return false;
        }

    }

}
