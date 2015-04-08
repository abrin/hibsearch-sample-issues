package org.digitalantiquity.hibsearch;

import javax.persistence.Entity;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

@Entity 
@Indexed
public class Novel extends Book {

    @Field
    private String subject;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
