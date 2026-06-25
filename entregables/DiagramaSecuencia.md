sequenceDiagram
    autonumber
    actor hilo1 as hilo1 :
    actor hilo2 as hilo2 :
    participant r as r : Monitor
    participant r1 as r1 : Semaphore (mutex)
    participant r2 as r2 : RdP
    participant r3 as r3 : Colas
    participant r4 as r4 : Politica

    Note over hilo1: hilo activo
    Note over hilo2: hilo dormido

    hilo1->>r: fireTransition()
    activate r
    r->>r1: acquire()
    activate r1
    r1-->>r: 
    deactivate r1
    r->>r: seguir=true
    
    loop seguir==true
        r->>r2: disparar()
        activate r2
        r2-->>r: seguir
        deactivate r2
        
        alt seguir==true
            r->>r2: sensibilizadas()
            activate r2
            r2-->>r: 
            deactivate r2
            
            r->>r3: quienesEstan()
            activate r3
            r3-->>r: 
            deactivate r3
            
            r->>r: m=Vs And Vc
            
            alt m<>0
                r->>r4: decidirTransicion()
                activate r4
                r4-->>r: 
                deactivate r4
                
                r->>r3: release()
                activate r3
                r3-->>hilo2: 
                Note over hilo2: se activa el hilo
                r3-->>r: 
                deactivate r3
                
                r-->>hilo1: 
                Note over hilo1: sale del monitor
            else m==0
                r->>r: seguir=false
            end
            
        else seguir==false
            r->>r1: release()
            activate r1
            r1-->>r: 
            deactivate r1
            
            r->>r3: acquire()
            activate r3
            r3-->>r: 
            deactivate r3
        end
    end
    
    r-->>hilo2: continua
    Note over hilo2: en este punto continua<br/>la ejecucion
    
    r->>r1: release()
    activate r1
    r1-->>r: 
    deactivate r1
    
    r-->>hilo2: 
    Note over hilo2: el hilo se va
    deactivate r