package org.cardanofoundation.metabus.common.enums;

/**
 * <p>
 * This POJO class defines every forms of the data in metabus system.
 *  
 * *Note: I think this name of the class need to be changed from `GroupType` to `DataType`
 * </p>
 * 
 * @Modified (Sotatek) joey.dao
 * @since 2023/08 
 */
public enum GroupType {
    /**
     * <p>
     * Single signature data type.
     * </p>
     */
    SINGLE_GROUP,

    /**
     * <p>
     * Multiple signatures data type.
     * </p>
     */
    MULTI_GROUP
}
