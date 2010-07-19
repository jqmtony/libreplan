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
package org.navalplanner.web.scenarios;

import java.util.Set;

import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.scenarios.bootstrap.IScenariosBootstrap;
import org.navalplanner.business.scenarios.daos.IScenarioDAO;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.web.users.services.CustomUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class CurrentUserScenarioAwareManager implements IScenarioManager {

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Override
    @Transactional(readOnly = true)
    public Scenario getCurrent() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        Scenario scenario = authentication == null ? scenariosBootstrap
                .getMain() : getScenarioFrom(authentication);

        if (scenario.getId() == null) {
            return scenario;
        }

        scenario = scenarioDAO.findExistingEntity(scenario.getId());
        forceLoad(scenario);
        return scenario;
    }

    private void forceLoad(Scenario scenario) {
        scenarioDAO.reattach(scenario);
        Set<Order> orders = scenario.getOrders().keySet();
        for (Order order : orders) {
            orderDAO.reattach(order);
            order.getName();
        }
    }

    private Scenario getScenarioFrom(Authentication authentication) {
        CustomUser user = (CustomUser) authentication.getPrincipal();
        return user.getScenario();
    }

}
