package com.jumpmind.symmetric.console.ui;

import java.io.File;
import java.util.List;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
/**
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ClientSymmetricEngine;
import org.jumpmind.symmetric.model.Node;


 * The main view contains a button and a click listener.
 */
@Route("")
public class MainView extends VerticalLayout {

    public MainView() {
        // Use TextField for standard text input
        TextField textField = new TextField("Nanoart");

        // Button click listeners can be defined as lambda expressions
        GreetService greetService = new GreetService();
        Button button = new Button("Symmetric OK", e ->  {
            // ISymmetricEngine engine = AbstractSymmetricEngine.findEngineByName(engineName);
            /*
             * 
            File sourceProperies = new File("master-nano190013.properties");
            ISymmetricEngine engine = new ClientSymmetricEngine(sourceProperies);
            Node targetNode = engine.getNodeService().findIdentity();
            List<Node> nodes = engine.getNodeService().findAllNodes();             * 
             * 
             */


            add(new Paragraph(greetService.greet(textField.getValue())));
        });

        // Theme variants give you predefined extra styles for components.
        // Example: Primary button is more prominent look.
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // You can specify keyboard shortcuts for buttons.
        // Example: Pressing enter in this view clicks the Button.
        button.addClickShortcut(Key.ENTER);

        // Use custom CSS classes to apply styling. This is defined in shared-styles.css.
        addClassName("centered-content");

        add(textField, button);
    }
}
