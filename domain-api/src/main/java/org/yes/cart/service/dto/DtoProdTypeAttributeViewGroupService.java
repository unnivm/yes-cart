/*
 * Copyright 2009 Igor Azarnyi, Denys Pavlov
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.yes.cart.service.dto;

import org.yes.cart.domain.dto.ProdTypeAttributeViewGroupDTO;
import org.yes.cart.exception.UnableToCreateInstanceException;
import org.yes.cart.exception.UnmappedInterfaceException;

import java.util.List;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 6/28/12
 * Time: 10:29 PM
 */
public interface DtoProdTypeAttributeViewGroupService  extends GenericDTOService<ProdTypeAttributeViewGroupDTO>  {


    /**
     * Get list of view groups, which belong to given product type.
     * @param productTypeId given product type.
     * @return list of attributes view group.
     */
    List<ProdTypeAttributeViewGroupDTO> getByProductTypeId(long productTypeId) throws UnmappedInterfaceException, UnableToCreateInstanceException;

}
