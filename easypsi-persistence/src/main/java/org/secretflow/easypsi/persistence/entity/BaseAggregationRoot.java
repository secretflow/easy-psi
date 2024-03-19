/*
 * Copyright 2023 Ant Group Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.secretflow.easypsi.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.secretflow.easypsi.persistence.converter.Boolean2IntConverter;

/**
 * Base aggregate root
 *
 * @author jiezi
 * @date 2023/5/30
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseAggregationRoot<A extends SuperBaseAggregationRoot<A>> extends SuperBaseAggregationRoot<A> {
    /**
     * Whether to delete tag
     */
    @Column(name = "is_deleted", nullable = false, length = 1)
    @Convert(converter = Boolean2IntConverter.class)
    Boolean isDeleted = false;
}
