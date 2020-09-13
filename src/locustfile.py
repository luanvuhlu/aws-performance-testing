# locustfile.py
import random
import string
import json
import resource
from locust import HttpUser, task


try:
    resource.setrlimit(resource.RLIMIT_NOFILE, (1000000, 1000000))
except:
    print("Couldn't raise resource limit")


def get_random_string(length):
    letters = string.ascii_lowercase
    return ''.join(random.choice(letters) for i in range(length))


class WebsiteUser(HttpUser):
    min_wait = 5000
    max_wait = 9000

    @task(1)
    def index(self):
        self.client.get('/')

    @task(2)
    def all(self):
        self.client.get('/students')

    @task(2)
    def get_one(self):
        self.client.get('/student/' + str(random.randint(1, 1000)))

    

    @task(2)
    def update_one(self):
        payload = {'name': get_random_string(1)}
        headers = {'content-type': 'application/json'}
        self.client.put('/students/' + str(random.randint(1, 1000)), headers=headers, data=json.dumps(payload))