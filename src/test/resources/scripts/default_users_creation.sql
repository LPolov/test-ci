INSERT INTO users (id, email, enabled, first_name, last_name, password, role)
VALUES('46a4f382-fafb-494c-a5ce-b14acbc307c4', 'validUser', 1, 'user', 'user', '$2a$10$SvCQN97uYNbi2PS11l3xfu/nPFdhuvCmiRwnDgAToyzWL0wldX8eq', 'USER');

INSERT INTO users (id, email, enabled, first_name, last_name, password, role)
VALUES('46a4f382-fafb-494c-a5ce-b14acbc307c5', 'disabledUser', 0, 'user', 'user', '$2a$10$SvCQN97uYNbi2PS11l3xfu/nPFdhuvCmiRwnDgAToyzWL0wldX8eq', 'USER');

INSERT INTO users (id, email, enabled, first_name, last_name, password, role, locked)
VALUES('46a4f382-fafb-494c-a5ce-b14acbc307c6', 'lockedUser', 1, 'user', 'user', '$2a$10$SvCQN97uYNbi2PS11l3xfu/nPFdhuvCmiRwnDgAToyzWL0wldX8eq', 'USER', 1);