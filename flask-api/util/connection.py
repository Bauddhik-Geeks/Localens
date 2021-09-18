import psycopg2
import os

conn = psycopg2.connect(os.environ['dbConnection'])
print(conn)
cur = conn.cursor()
cur.execute("use localens") 