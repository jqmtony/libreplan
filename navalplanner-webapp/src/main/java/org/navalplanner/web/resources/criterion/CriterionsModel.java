/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.resources.criterion;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.NonUniqueResultException;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.daos.IEntitySequenceDAO;
import org.navalplanner.business.common.entities.EntityNameEnum;
import org.navalplanner.business.common.entities.EntitySequence;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for criterions. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component("criterionsModel_V2")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/resources/criterions/criterions-V2.zul")
public class CriterionsModel implements ICriterionsModel {

    private static final Log log = LogFactory.getLog(CriterionsModel.class);

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IEntitySequenceDAO entitySequenceDAO;

    private CriterionType criterionType;

    private Criterion criterion;

    private ICriterionTreeModel criterionTreeModel;

    private String oldCodeCriterionType;

    private Map<Criterion, String> oldCriterionTypeCodeChildren = new HashMap<Criterion, String>();

    private Boolean generateCodeOldCriterionType;

    @Override
    @Transactional(readOnly = true)
    public List<CriterionType> getTypes() {
        return criterionTypeDAO.getCriterionTypes();
    }

    @Override
    public ICriterionType<?> getCriterionType() {
        return criterionType;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Criterion> getCriterionsFor(ICriterionType<?> type) {
        return criterionDAO.findByType(type);
    }

    @Override
    public Criterion getCriterion() {
        return criterion;
    }

    @Override
    public ICriterionTreeModel getCriterionTreeModel() {
        return criterionTreeModel;
    }

    @Override
    @Transactional(readOnly=true)
    public void prepareForCreate() {
        this.criterionType = CriterionType.create("");
        initializeCriterionTypeCode();
        this.criterionTreeModel = new CriterionTreeModel(criterionType);
    }

    @Override
    @Transactional(readOnly = true)
    public void initializeCriterionTypeCode() {
        boolean generateCode = configurationDAO.getConfiguration()
                .getGenerateCodeForCriterion();
        if (generateCode) {
            setDefaultCriterionTypeCode();
        }
        this.criterionType.setGenerateCode(generateCode);
    }

    @Override
    public void setCodeAutogenerated(boolean codeAutogenerated)
            throws ConcurrentModificationException {
        if (criterionType != null) {
            if (criterionType.isNewObject()) {
                setDefaultCriterionTypeCode();
            } else {
                if (isGenerateCodeOldCriterionType()) {
                    restoreOldCodes();
                } else {
                    setDefaultCriterionTypeCode();
                }
            }
            criterionType.setGenerateCode(codeAutogenerated);
        }
    }

    private void setDefaultCriterionTypeCode()
            throws ConcurrentModificationException {
        String code = entitySequenceDAO
                .getNextEntityCode(EntityNameEnum.CRITERION);
        if (code == null) {
            throw new ConcurrentModificationException(
                    _("Could not get criterion type code, please try again later"));
        }
        this.criterionType.setCode(code);
    }

    private void generateCriterionCodes() {
        int numberOfDigits = 5;
        try {
            EntitySequence entitySequence = entitySequenceDAO
                    .getActiveEntitySequence(EntityNameEnum.CRITERION);
            numberOfDigits = entitySequence.getNumberOfDigits();
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NonUniqueResultException e) {
            throw new RuntimeException(e);
        }

        criterionTreeModel.regenerateCodeForUnsavedCriteria(numberOfDigits);
    }

    @Override
    public void prepareForCreate(CriterionType criterionType) {
        this.criterionType = criterionType;
        this.criterion = (Criterion) criterionType
                .createCriterionWithoutNameYet();
    }

    @Override
    @Transactional
    public void prepareForRemove(CriterionType criterionType) {
        this.criterionType = criterionType;
        criterionTypeDAO.reattach(criterionType);
        criterionType.getCriterions().size();
    }

    @Override
    @Transactional(readOnly = true)
    public void prepareForEdit(CriterionType criterionType) {
        Validate.notNull(criterionType);
        this.criterionType = getFromDB(criterionType);
        this.criterionTreeModel = new CriterionTreeModel(this.criterionType);
        initOldCodes();
    }

    private void initOldCodes() {
        if (criterionType != null) {
            setOldCodeCriterionType(criterionType.getCode());
            for (Criterion criterion : criterionType.getCriterions()) {
                oldCriterionTypeCodeChildren
                        .put(criterion, criterion.getCode());
            }
            setGenerateCodeOldCriterionType(criterionType.isCodeAutogenerated());
        }
    }

    private void restoreOldCodes() {
        criterionType.setCode(getOldCodeCriterionType());
        for (Criterion criterion : oldCriterionTypeCodeChildren.keySet()) {
            criterion.setCode(oldCriterionTypeCodeChildren.get(criterion));
        }
    }

    @Override
    @Transactional
    public void remove(CriterionType criterionType) {
        try {
            criterionTypeDAO.remove(criterionType.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional(readOnly = true)
    private CriterionType getFromDB(CriterionType criterionType) {
        try {
            return criterionTypeDAO.find(criterionType.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ICriterionType<?> getTypeFor(Criterion criterion) {
        for (ICriterionType<?> each : getTypes()) {
            if (each.contains(criterion)) {
                return each;
            }
        }
        throw new RuntimeException(_("{0} not found type for criterion ", criterion));
    }

    @Override
    @Transactional
    public void saveCriterionType() throws ValidationException {
        if (criterionType.isCodeAutogenerated()) {
            generateCriterionCodes();
        }
        criterionTreeModel.saveCriterions(criterionType);
        criterionTypeDAO.save(criterionType);
    }

    @Override
    public boolean isEditing() {
        return criterion != null;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isApplyableToWorkers(Criterion criterion) {
        ICriterionType<?> type = getTypeFor(criterion);
        return type != null && type.criterionCanBeRelatedTo(Worker.class);
    }

    @Override
    @Transactional(readOnly = true)
    public <T extends Resource> List<T> getResourcesSatisfyingCurrentCriterionOfType(
            Class<T> klass) {
        if (criterion == null) {
            return new ArrayList<T>();
        }
        return getResourcesSatisfying(klass, criterion);
    }

    private <T extends Resource> List<T> getResourcesSatisfying(
            Class<T> resourceType, Criterion criterion) {
        Validate.notNull(resourceType, _("ResourceType must be not-null"));
        Validate.notNull(criterion, _("Criterion must be not-null"));
        List<T> result = new ArrayList<T>();
        for (T r : resourceDAO.list(resourceType)) {
            if (criterion.isSatisfiedBy(r)) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Worker> getAllWorkers() {
        return resourceDAO.getWorkers();
    }

    @Override
    public boolean getAllowHierarchy(){
        return this.criterionType.allowHierarchy();
    }

    @Override
    @Transactional(readOnly = true)
    public void reloadCriterionType() {
        this.criterionType = getFromDB(criterionType);
        this.criterionTreeModel = new CriterionTreeModel(this.criterionType);
        this.initOldCodes();
    }

    @Override
    @Transactional(readOnly = true)
    public int numberOfRelatedEntities(Criterion criterion) {
        if (criterion.isNewObject()) {
            return 0;
        }
        return criterionDAO.numberOfRelatedRequirements(criterion)
                + criterionDAO.numberOfRelatedSatisfactions(criterion);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDeletable(Criterion criterion) {
        return criterion.isNewObject()
                || (criterion.getChildren().isEmpty() && (numberOfRelatedEntities(criterion) == 0));
    }

    @Override
    public void addForRemoval(Criterion criterion) {
        criterionType.getCriterions().remove(criterion);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDeletable(CriterionType criterionType) {
        for (Criterion each : criterionType.getCriterions()) {
            if (numberOfRelatedEntities(each) != 0) {
                return false;
            }
        }
        return true;
    }

    private void setOldCodeCriterionType(String oldCodeCriterionType) {
        this.oldCodeCriterionType = oldCodeCriterionType;
    }

    private String getOldCodeCriterionType() {
        return oldCodeCriterionType;
    }

    private void setGenerateCodeOldCriterionType(Boolean generatedCode) {
        this.generateCodeOldCriterionType = generatedCode;
    }

    private Boolean isGenerateCodeOldCriterionType() {
        return (generateCodeOldCriterionType != null) ? generateCodeOldCriterionType
                : false;
    }

}
