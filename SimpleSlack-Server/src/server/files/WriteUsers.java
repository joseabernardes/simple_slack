/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.Semaphore;
import model.User;

/**
 * <h3>
 * ESTG - Escola Superior de Tecnologia e Gestão<br>
 * SOCP - Trabalho Pratico<br>
 * </h3>
 * <p>
 * <strong>Nome:</strong> Joel Ribeiro Pereira<br>
 * <strong>Número:</strong> 8150138<br>
 * <strong>Turma:</strong> LEI2T3<br>
 * <p>
 * <strong>Nome:</strong> José Paulo de Almeida Bernardes<br>
 * <strong>Número:</strong> 8150148<br>
 * <strong>Turma:</strong> LEI2T3<br>
 * </p>
 * <p>
 * <strong>Descrição: </strong><br>
 * Classe que representa um Thread que guarda o stock de produtos no ficheiro
 * </p>
 */
public class WriteUsers extends Thread{

    private final Semaphore semaphore;
    private final List<User> users;

    public WriteUsers(Semaphore semaphore, List<User> users) {
        this.semaphore = semaphore;
        this.users = users;
    }

    @Override
    public void run() {
        try {
            semaphore.acquire();
            File ficheiro = new File("src/users.txt");
            ObjectOutputStream output;
            output = new ObjectOutputStream(new FileOutputStream(ficheiro));
            output.writeObject((List<User>) users);
        } catch (IOException | InterruptedException ex) {
            System.out.println("Erro a gravar");
        } finally {
            semaphore.release();
        }
    }
}
