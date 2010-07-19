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

package org.navalplanner.web.print;

import static org.zkoss.ganttz.i18n.I18nHelper._;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class CutyCaptTimeout extends Thread {

    long timeout;

    private static final Log LOG = LogFactory.getLog(CutyPrint.class);

    CutyCaptTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void run() {
        try {
            sleep(timeout);
            Runtime.getRuntime().exec("killall CutyCapt");
        } catch (Exception e) {
            LOG.error(_("CutycaptTimeout thread exception"), e);
        }
    }
}
