package com.jumpmind.symmetric.console.ui.data;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.vaadin.hilla.Nonnull;
/*
 * We are unable to use Symmetric's Node class here, so we will use a simple class to represent a node.
 * 
 */
@Entity
public class VNNode {
    @Id
    @GeneratedValue
    private Long id;
    private String name;

    public Long getId() {
        return id;
      }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
