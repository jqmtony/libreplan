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

package org.navalplanner.business.materials.bootstrap;

import org.navalplanner.business.calendars.entities.CalendarExceptionType;
import org.navalplanner.business.materials.daos.IMaterialCategoryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the default {@link CalendarExceptionType}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Component
@Scope("singleton")
public class MaterialCategoryBootstrap implements IMaterialCategoryBootstrap {

    @Autowired
    private IMaterialCategoryDAO materialCategoryDAO;

    @Override
    @Transactional
    public void loadRequiredData() {
        for (PredefinedMaterialCategories predefinedMaterialCategory : PredefinedMaterialCategories
                .values()) {
            if (!materialCategoryDAO
                    .existsMaterialCategoryWithNameInAnotherTransaction(predefinedMaterialCategory
                            .getName())) {
                materialCategoryDAO.save(predefinedMaterialCategory
                        .createMaterialCategory());
            }
        }
    }

}
