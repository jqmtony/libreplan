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

package org.navalplanner.business.costcategories.entities;

import java.math.BigDecimal;

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.costcategories.daos.IHourCostDAO;

/**
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public class HourCost extends IntegrationEntity {

    private BigDecimal priceCost;

    private LocalDate initDate;

    private LocalDate endDate;

    private TypeOfWorkHours type;

    private CostCategory category;

    // Default constructor, needed by Hibernate
    protected HourCost() {

    }

    public static HourCost createUnvalidated(String code, BigDecimal priceCost,
            LocalDate initDate) {
        HourCost hourCost = create(new HourCost(), code);
        if (priceCost != null) {
            hourCost.setPriceCost(priceCost);
        }
        if (initDate != null) {
            hourCost.setInitDate(initDate);
        }
        return hourCost;
    }

    public void updateUnvalidated(BigDecimal priceCost, LocalDate initDate) {
        if (priceCost != null) {
            this.priceCost = priceCost;
        }
        if (initDate != null) {
            this.initDate = initDate;
        }
    }

    public static HourCost create(BigDecimal priceCost, LocalDate initDate) {
        return (HourCost) create(new HourCost(priceCost, initDate));
    }

    public static HourCost create() {
        return (HourCost) create(new HourCost());
    }

    protected HourCost(BigDecimal priceCost, LocalDate initDate) {
        this.priceCost = priceCost;
        this.initDate = initDate;
    }

    @NotNull(message = "price cost not specified")
    public BigDecimal getPriceCost() {
        return priceCost;
    }

    public void setPriceCost(BigDecimal priceCost) {
        this.priceCost = priceCost;
    }

    @NotNull(message = "init date not specified")
    public LocalDate getInitDate() {
        return initDate;
    }

    public void setInitDate(LocalDate initDate) {
        this.initDate = initDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @NotNull(message = "type of work hours not specified")
    public TypeOfWorkHours getType() {
        return type;
    }

    public void setType(TypeOfWorkHours type) {
        this.type = type;
    }

    @NotNull(message = "cost category not specified")
    public CostCategory getCategory() {
        return category;
    }

    public void setCategory(CostCategory category) {
        CostCategory oldCategory = this.category;
        this.category = category;
        if(oldCategory!=null)
            oldCategory.removeHourCost(this);
        if(category!=null && !category.getHourCosts().contains(this))
            category.addHourCost(this);
    }

    public boolean isActiveAtDate(LocalDate date) {
        if ((date.isAfter(this.getInitDate()))
                && (!date.isBefore(this.getInitDate()))) {
            return true;
        }
        return false;
    }

    @AssertTrue(message="The end date cannot be before the init date")
    public boolean checkPositiveTimeInterval() {
        if (initDate == null) {
            return true;
        }
        if (endDate == null) {
            return true;
        }
        return (endDate.isAfter(initDate) || initDate.equals(endDate));
    }

    @Override
    protected IHourCostDAO getIntegrationEntityDAO() {
        return Registry.getHourCostDAO();
    }
}
