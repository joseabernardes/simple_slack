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
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Semaphore;
import model.Database;
import model.GroupServer;
import model.UserServer;

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
public class WriteDatabase extends Thread {

    private final Semaphore semaphore;
    private final Database database;

    public WriteDatabase(Semaphore semaphore, Database database) {
        this.semaphore = semaphore;
        this.database = database;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(10000);
                semaphore.acquire();
                File ficheiro = new File("database.txt");
                ObjectOutputStream output;
                output = new ObjectOutputStream(new FileOutputStream(ficheiro));
                output.writeObject((Database) database);
                System.out.println("Database gravada @" + LocalDateTime.now() );
            } catch (IOException | InterruptedException ex) {
                System.out.println("Erro a gravar database");
            } finally {
                semaphore.release();
            }
        }
    }
}
