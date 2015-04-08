package org.digitalantiquity.hibsearch;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.search.annotations.DocumentId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Persistable extends Serializable {

    Long getId();

    void setId(Long number);

    @MappedSuperclass
    public abstract static class Base implements Persistable {

        private static final long serialVersionUID = -458438238558572364L;


        @Transient
        protected final transient Logger logger = LoggerFactory.getLogger(getClass());

        /**
         * Uses GenerationType.IDENTITY, which translates to the (big)serial column type for
         * hibernate+postgres, i.e., one sequence table per entity type
         */
        @Id
        @DocumentId
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id = -1L;
        
        @Override
        public Long getId() {
            return id;
        }
        
        @Override
        public void setId(Long number) {
            this.id = id;
        }
    }
}
