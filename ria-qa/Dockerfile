FROM nexus.ria.ee:8500/ramentrum/poetry-python

COPY . .

ENV PATH=${PATH}:/root/.poetry/bin
RUN poetry install
