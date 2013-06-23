
import socket
import io
import sys

HOST = ''                 # Symbolic name meaning the local host
PORT = 50007              # Arbitrary non-privileged port
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind((HOST, PORT))
s.listen(1)

print "listening ..."

conn, addr = s.accept()
print ('connected by', addr)


try:
    f = open("test_263.mp4", "wb")
    while 1:
        data = conn.recv(1024)
        print "recieved data"
        f.write(data)
        if not data:
            print "closing file"
            f.close()
            break

    conn.close()

except IOError as e:
    print "I/O error({0}): {1}".format(e.errno, e.strerror)
except ValueError:
    print "Could not convert data to an integer."
except:
    print "Unexpected error:", sys.exc_info()[0]
    raise


"""
UDP

import socket
import sys

HOST = ''                 # Symbolic name meaning the local host
PORT = 50007              # Arbitrary non-privileged port
s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
s.bind((HOST, PORT))

print "listening ..."

while 1:
    data, address = s.recvfrom(1024)
    print "received data from " + str(address)
    if not data:
        break
"""
